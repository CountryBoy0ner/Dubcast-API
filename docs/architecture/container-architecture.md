# Dubcast Containerization Overview

This document provides a concise overview of how the **Dubcast** project is containerized.
It is intended as a quick reference for reviewers and developers.

## Images

### Backend (`servicesite-backend`)

- **Build type:** multi-stage Docker build (Maven build stage → runtime stage)
- **Runtime base image:** `mcr.microsoft.com/playwright/java:<version>-jammy`  
  Used because the project includes SoundCloud parsing via **Playwright** (browser runtime required).
- **Exposed port (inside container):** `8080`
- **Run mode:** stateless application container (data stored in PostgreSQL)

### Database (`servicesite-db`)

- **Image:** `postgres:16-alpine`
- **Persistence:** Docker volume mounted to `/var/lib/postgresql/data`
- **Internal port:** `5432` (typically not exposed externally)

## Containers

### `servicesite-backend`

- **Ports:** `8089:8080` (host → container)
- **Depends on:** `servicesite-db` being healthy (`depends_on` + healthcheck)
- **Healthcheck:** `GET /actuator/health` should report `UP`
- **Configuration:** provided via `.env.docker` and environment variables:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET` (from env file, not hardcoded)
  - other application settings as needed

### `servicesite-db`

- **Volumes:** `dubcast_db-data:/var/lib/postgresql/data`
- **Healthcheck:** `pg_isready`

## Network and Volume

- **Network:** `app-net` (bridge) for backend ↔ database communication
- **Volume:** `dubcast_db-data` for PostgreSQL persistence

## How to Run

```bash
docker compose up --build
```

## Notes

- Resource limits under `deploy.resources.limits` are included for documentation/review purposes.
  In standard Docker Compose (non-Swarm), those limits may not be enforced by default.
