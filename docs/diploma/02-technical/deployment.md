# Deployment & DevOps

This document describes how **Dubcast** is run locally (for demo/review) and how CI/CD is implemented.

> Current scope note: the diploma project focuses on **reproducible local deployment** (Docker Compose) and **automated CI/CD**. A public “staging/production” hosting environment is not required.

---

## Infrastructure

### Deployment Architecture (local demo)

- **Frontend** (Angular SPA) is typically run in development mode (`ng serve`) and communicates with backend via HTTP.
- **Backend** (Spring Boot) runs as a Docker container.
- **Database** (PostgreSQL) runs as a Docker container.
- Backend publishes an image to **GHCR** for reproducible runs.

```
┌──────────────────────────┐          HTTP (SPA)           ┌──────────────────────────┐
│  Web Browser             │  ───────────────────────────▶ │  Angular Dev Server       │
│  (Listener UI)           │                               │  (ng serve, :4200)        │
└──────────────────────────┘                               └───────────┬──────────────┘
                                                                        │
                                                                        │ HTTP (REST / WS)
                                                                        ▼
                                                           ┌──────────────────────────┐
                                                           │  Spring Boot Backend      │
                                                           │  (Docker, :8089)          │
                                                           └───────────┬──────────────┘
                                                                       │ JDBC
                                                                       ▼
                                                           ┌──────────────────────────┐
                                                           │ PostgreSQL                │
                                                           │ (Docker, :5432)           │
                                                           └──────────────────────────┘

Admin management (API-only):
┌──────────────────────────┐           HTTP (REST)          ┌──────────────────────────┐
│ Admin in Web Browser      │  ───────────────────────────▶ │ Swagger UI (same backend) │
│ (Swagger UI)              │                               │ /swagger-ui/index.html    │
└──────────────────────────┘                               └──────────────────────────┘

External integration:
Spring Boot Backend  ──HTTP──▶ SoundCloud pages / oEmbed (metadata parsing / import flows)
```

### Environments

| Environment | URL | Notes / Branch |
|-------------|-----|----------------|
| **Local Development** | Frontend: `http://localhost:4200`<br>Backend (compose): `http://localhost:8089` | Branch: any (`feature/*`, `develop`, `main`) |
| **CI/CD Artifacts** | GHCR image + workflow artifacts | Image publish happens only from `main` on push/manual |
| **Staging / Production** | N/A (not required for current diploma scope) | Can be added later by deploying GHCR image |

---

## CI/CD Pipeline (GitHub Actions)

### Pipeline Overview

Two jobs are used:

1. **CI** runs for every push/PR (main + develop): formatting, build/tests, artifacts, security scan (repo).
2. **CD** runs only on **push to main** (or manual dispatch): builds & publishes Docker image to **GHCR**, then scans the image.

```
┌──────────────┐
│ push / PR     │
└──────┬────────┘
       ▼
┌──────────────────────────────────────────────────────────────┐
│ CI job (always on main/develop push + PR):                    │
│ 1) Spotless format check                                      │
│ 2) Maven build + unit tests + JaCoCo coverage gate            │
│ 3) Upload artifacts (JAR, JaCoCo, Surefire reports)           │
│ 4) Trivy FS scan (repo)                                       │
└───────────┬──────────────────────────────────────────────────┘
            │ (only on main push / workflow_dispatch)
            ▼
┌──────────────────────────────────────────────────────────────┐
│ CD job (main only):                                           │
│ 1) Docker buildx build                                        │
│ 2) Push image to GHCR (tags: sha + latest)                    │
│ 3) Trivy image scan (container image)                         │
└──────────────────────────────────────────────────────────────┘
```

### What is checked in CI

| Step | Tooling | What it does |
|------|---------|--------------|
| Format gate | Spotless | Fails the pipeline if formatting differs from the agreed style. |
| Build & tests | Maven (`.\mvnw.cmd -ntp clean verify`) | Compiles project and runs tests. |
| Coverage gate | JaCoCo | Enforces minimum coverage (service layer) before merge. |
| Artifacts | upload-artifact | Uploads JAR + test reports to workflow artifacts (useful for reviewers). |
| Repo security scan | Trivy (FS) | Scans repository dependencies for **HIGH/CRITICAL** vulnerabilities and fails on findings. |

### What happens in CD (main only)

| Step | Tooling | What it does |
|------|---------|--------------|
| Build & push image | Docker buildx + GHCR | Builds Docker image and pushes to GitHub Container Registry. |
| Image security scan | Trivy (image) | Scans the published image and fails on **CRITICAL** vulnerabilities. |

> Note about naming in the workflow:
> - The **FS scan** step label says “CRITICAL”, but it is configured for `HIGH,CRITICAL`.
> - The **image scan** step label says “HIGH/CRITICAL”, but it is configured for `CRITICAL`.  
    > The configuration is what matters; the labels can be updated for clarity.

---

## Environment Variables

### Backend (.env / .env.docker)

In Docker Compose, backend and DB are configured via `.env.docker` (recommended) to avoid committing secrets.

Typical variables:

| Variable | Description | Required | Example                                         |
|----------|-------------|----------|-------------------------------------------------|
| `DB_NAME` | PostgreSQL database name | Yes | `dubcast`                                       |
| `DB_USERNAME` | PostgreSQL user | Yes | `dubcast`                                       |
| `DB_PASSWORD` | PostgreSQL password | Yes | `change_me`                                     |
| `DB_PORT` | PostgreSQL port | No (default 5432) | `5432`                                          |
| `DB_URL` | JDBC URL for backend | Yes (computed in compose) | `jdbc:postgresql://servicesite-db:5432/dubcast` |
| `SERVER_PORT` | Backend container port | No | `8080`                                          |
| `JWT_SECRET` | JWT signing secret | Yes | stored in `.env(.docker)` / GitHub Secrets      |

### Frontend

Frontend usually needs backend base URL:

| Variable | Description | Example |
|----------|-------------|---------|
| `API_BASE_URL` (or Angular env) | Backend URL used by Angular | `http://localhost:8089` |

---

## How to Run Locally

### Prerequisites

- Docker + Docker Compose
- Java 17 + Maven (optional, if running backend without Docker)
- Node.js + npm (for Angular UI)

### Option A: Run backend + database (Docker Compose)

1) Create `.env.docker` in backend repo (do **not** commit it):

```bash
DB_NAME=dubcast
DB_USERNAME=dubcast
DB_PASSWORD=change_me
DB_PORT=5432
JWT_SECRET=change_me
```

2) Start containers:

```bash
docker compose up --build
```

3) Verify:

- Backend health: `http://localhost:8089/actuator/health`
- Swagger UI: `http://localhost:8089/swagger-ui/index.html`

### Option B: Run frontend (Angular)

From the UI repo:

```bash
npm install
npm start
```

Open: `http://localhost:4200`

> Ensure the frontend is configured to call the backend (e.g., proxy config or Angular environment base URL).

---

## Monitoring & Logging

- **Logs**: standard container logs (`docker compose logs -f`)
- Backend includes structured logging via `logstash-logback-encoder` (useful for readable JSON-like logs).
- No external monitoring (APM/Sentry) is used in the current diploma scope.
