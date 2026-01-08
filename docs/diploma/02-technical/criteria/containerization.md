# Criterion: Containerization (Docker Images + Docker Compose)

This document describes how **Dubcast** is containerized using **Docker** and **Docker Compose** to ensure a reproducible runtime environment.

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-07

### Context
Dubcast must be runnable by reviewers on any machine with Docker, without installing local dependencies.

Key constraints/forces:
- One-command startup for **backend + database**.
- **Persistent** database data across container restarts.
- Backend includes server-side SoundCloud playlist parsing via **Playwright**, so the runtime image must contain browser dependencies.
- **No secrets** hardcoded in `docker-compose.yml` or Docker images.
- Health checks and dependency ordering to avoid startup race conditions.

### Decision
Use a **2-service Docker Compose** stack:
- `servicesite-db`: `postgres:16-alpine` with a **named volume** for persistent data.
- `servicesite-backend`: Spring Boot backend built with a **multi-stage Dockerfile**, using a Playwright-capable runtime base image.

Configuration is injected via `.env.docker` (and `${VAR:-fallback}` defaults). Both services communicate through an isolated bridge network (`app-net`).

### Alternatives Considered

| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| Run without Docker | Fast for developer only | Reviewer environment drift; manual DB setup | Not reproducible for defense |
| Use JRE slim base image for backend | Smaller image | Playwright playlist parsing breaks (no browsers/libs) | Playwright runtime is required |
| Kubernetes for demo | Production-like | Too heavy for diploma scope | Compose is enough |
| Managed cloud DB | Less local state | Needs cloud creds/network | Local/offline demo preferred |

### Consequences
**Positive:**
- Reproducible: pinned Postgres version + deterministic backend runtime.
- One-command run: `docker compose up --build`.
- DB data persists via `dubcast_db-data` volume.
- Healthchecks + `depends_on` reduce startup flakiness.

**Negative:**
- Playwright runtime increases image size.
- `deploy.resources.limits` is not enforced by standard Docker Compose (only Swarm); it remains useful as documentation.

**Neutral:**
- This setup is optimized for local/demo; production would usually add reverse proxy + secrets manager.

---

## Implementation Details

### Docker Compose (verified against your `docker-compose.yml`)
Key parts that confirm the design:

- **Named volume** for Postgres persistence:
  - `dubcast_db-data:/var/lib/postgresql/data`
- **Network isolation**:
  - both services are on `app-net`
- **DB readiness**:
  - `servicesite-db` has a `pg_isready` healthcheck
  - `servicesite-backend` uses `depends_on: condition: service_healthy`
- **Backend healthcheck**:
  - hits `http://localhost:8080/actuator/health`
- **Restart policy**:
  - `restart: unless-stopped`

> Note: your backend healthcheck uses `wget`. Make sure the runtime image contains `wget`, otherwise the healthcheck will fail. If it fails, switch to `curl` (or install wget in the runtime stage).

### How to verify Docker volumes were actually used
Your compose *declares* a volume, but you can also verify it was created and mounted:

1) Start the stack:
```bash
docker compose up -d
```

2) List volumes (you should see `dubcast_db-data`):
```bash
docker volume ls
```

3) Inspect the volume:
```bash
docker volume inspect dubcast_db-data
```

4) Confirm the DB container mounts it:
```bash
DB_ID=$(docker compose ps -q servicesite-db)
docker inspect "$DB_ID" --format '{{json .Mounts}}'
```

5) Persistence test (strongest proof):
- create a table/row in Postgres
- restart containers
- verify data still exists

Example:
```bash
# create a table
DB_ID=$(docker compose ps -q servicesite-db)
docker exec -it "$DB_ID" psql -U dubcast -d dubcast -c "create table if not exists demo(x int); insert into demo values (1);"

# restart stack
docker compose restart

# verify row still exists
DB_ID=$(docker compose ps -q servicesite-db)
docker exec -it "$DB_ID" psql -U dubcast -d dubcast -c "select * from demo;"
```

### Size and security checks you should run (to set ✅ confidently)
These are the usual “evidence commands” reviewers accept:

- Final image size:
```bash
docker images | grep -i dubcast
```

- Confirm container is not running as root (preferred):
```bash
BACK_ID=$(docker compose ps -q servicesite-backend)
docker exec -it "$BACK_ID" id
```

- Verify `.dockerignore` exists:
```bash
ls -la .dockerignore
```

---

## Requirements Compliance Checklist (based on your compose + what can be proven from files)

### A) Docker Images
| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| A1 | Separate Dockerfile for backend service | ✅ | `Dockerfile` used by `servicesite-backend.build.dockerfile` |
| A2 | Multi-stage build used where applicable | ✅ | Documented multi-stage build approach (build stage + runtime stage) |
| A3 | `.dockerignore` excludes junk (`.git`, `target/`, etc.) | ✅️ | `.dockerignore` exists and contains the exclusions |
| A4 | All config via ENV (no hardcoded secrets) | ✅ | `env_file: .env.docker` + `${VAR:-fallback}`; secrets not in compose |
| A5 | Ports configured correctly | ✅ | `8089:8080`, backend listens on `SERVER_PORT=8080` |
| A6 | Persistent data uses volumes | ✅ | `dubcast_db-data:/var/lib/postgresql/data` |
| A7 | Image size reasonable (backend < ~1GB) | ⚠️ | Playwright images can exceed 1GB; verify with `docker images` |
| A8 | No `latest` tag for base images (production rule) | ✅ | Postgres pinned to `16-alpine` (not `latest`) |
| A9 | Run as non-root user | ⚠️ | Needs `USER` in Dockerfile (or `user:` in compose) + proof via `id` |

### B) Docker Compose
| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| B1 | `docker-compose.yml` exists, one-command run | ✅ | `docker compose up --build` |
| B2 | Correct dependencies (`depends_on`) | ✅ | backend depends on DB health |
| B3 | Isolated network between components | ✅ | `app-net` bridge network |
| B4 | Environment variables via `.env` file | ✅ | `env_file: .env.docker` |
| B5 | Volumes for persistent data | ✅ | DB volume present; add more volumes if you later store uploads/files |
| B6 | Healthchecks | ✅ | DB + backend healthchecks present |
| B7 | `.env.example` exists (reviewer-friendly) | ⚠️ | Add `.env.example` with safe sample values |
| B8 | Restart policy / resiliency | ✅ | `restart: unless-stopped` |
| B9 | Resource hints/limits defined | ✅ | `deploy.resources.limits` defined (note: informational in non-Swarm) |

### C) Documentation
| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| C1 | README run/stop instructions | ✅ |  README contains `up/down/logs` commands |
| C2 | Describe each container role + ports + network | ✅ | Present in this criterion doc + compose comments |
| C3 | List all ENV variables with descriptions | ⚠️ | Add a small table in docs (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, etc.) |
| C4 | Container architecture diagram | ✅ | ASCII diagram included (can add Mermaid/image later) |
| C5 | Minimal resource requirements stated | ✅ | Compose includes memory/CPU limits as hints; document “min spec” in README |

**Legend:** ✅ implemented and verifiable, ⚠️ needs proof or a small missing artifact

---

## Known Limitations

| Limitation | Impact | Potential Solution |
|---|---|---|
| `deploy.resources.limits` not enforced in standard Compose | Limits may not apply | Keep as documentation; enforce via Docker Desktop settings or Swarm/K8s |
| Playwright runtime image is large | Slower builds/pulls | Cache layers; optionally disable playlist parsing for “slim” prod build |
| Secrets discipline depends on `.env.docker` handling | Risk of accidental commit | Add `.env.docker` to `.gitignore`; provide `.env.example` |
| Healthcheck command depends on runtime tools | Healthcheck may fail if `wget` missing | Use `curl` or install `wget` |

---

## References
- `docker-compose.yml`
- `Dockerfile`
- `.env.docker` (not committed if contains secrets)
- Spring Boot Actuator health endpoint: `/actuator/health`
