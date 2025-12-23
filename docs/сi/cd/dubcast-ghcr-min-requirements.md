# Dubcast Backend (GHCR) — Minimal Requirements & Run Guide

This document describes the **minimum** needed to run the Dubcast backend using the **prebuilt GHCR image** via `docker-compose.ghcr.yml`.

---

## 1) Minimal requirements

### OS
- Windows 10/11 (PowerShell) or Linux/macOS

### Required software
- **Docker Desktop** (or Docker Engine) with **Docker Compose v2**
- Internet access (to pull images from GHCR)

### Ports
- `8089` must be free on the host machine (used for backend HTTP access)
- Docker internal Postgres uses `5432` **inside the container network** (no host port required)

---

## 2) Files that must exist

### `docker-compose.ghcr.yml`
Your compose file should load variables from `.env.docker` using `env_file` (example structure):

```yaml
services:
  servicesite-db:
    image: postgres:16-alpine
    env_file:
      - .env.docker
    networks:
      default:
        aliases:
          - db
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 5s
      timeout: 3s
      retries: 5
    volumes:
      - servicesite-db-data:/var/lib/postgresql/data

  servicesite-backend:
    image: ghcr.io/countryboy0ner/dubcast-api:latest
    env_file:
      - .env.docker
    depends_on:
      servicesite-db:
        condition: service_healthy
    ports:
      - "8089:8080"
    restart: unless-stopped

volumes:
  servicesite-db-data:
```

### `.env.docker`
A minimal `.env.docker` example (adjust values as needed):

```env
# Postgres container
POSTGRES_DB=DubcastDataBase
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123

# Backend datasource (IMPORTANT: host must be "db" because of network alias)
DB_URL=jdbc:postgresql://db:5432/DubcastDataBase
DB_USERNAME=postgres
DB_PASSWORD=123

# Backend http port inside container (the app listens on 8080)
SERVER_PORT=8080

# App settings
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=super_super_long_secret_key_32+chars
TIME_STAMP=Europe/Vilnius

# SoundCloud
SOUNDCLOUD_CLIENT_ID=YOUR_CLIENT_ID
SOUNDCLOUD_API_BASE_URL=https://api-v2.soundcloud.com
```

> Notes:
> - `DB_URL` must reference **`db`**, not `localhost`.
> - Host port is fixed to **8089** by compose (`8089:8080`). The app still listens on **8080** inside the container.
> - You can keep your local dev `.env` separate — this file is only for GHCR compose runs.

---

## 3) Run commands (Windows PowerShell / terminal)

From the folder that contains `docker-compose.ghcr.yml` and `.env.docker`:

### Clean start (recommended)
```powershell
docker compose -f docker-compose.ghcr.yml down -v
docker compose -f docker-compose.ghcr.yml up -d --force-recreate
```

### Check containers
```powershell
docker compose -f docker-compose.ghcr.yml ps
```

### Follow backend logs
```powershell
docker compose -f docker-compose.ghcr.yml logs -f servicesite-backend
```

---

## 4) How to verify it works

### A) Verify containers + port mapping
```powershell
docker ps --filter "name=servicesite-backend" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```
Expected: something like `0.0.0.0:8089->8080/tcp` and status `Up ...`

### B) Check API / Swagger availability

**PowerShell tip:** `curl` in PowerShell can map to `Invoke-WebRequest`, so use `curl.exe` explicitly.

```powershell
curl.exe -I http://localhost:8089/
curl.exe -I http://localhost:8089/swagger-ui/index.html
curl.exe -I http://localhost:8089/v3/api-docs
```

Expected:
- `/` returns `200`
- `/swagger-ui/index.html` returns `200`
- `/v3/api-docs` returns JSON (`200`)

---

## 5) Common pitfalls (quick fixes)

### “The POSTGRES_USER variable is not set” / “no port specified 8089:<empty>”
Reason: you ran `docker compose ...` **without** the env file loaded, so compose couldn't expand variables used in the YAML.

Fix:
- Ensure `env_file: - .env.docker` is present in the compose services that need it.
- Run commands from the folder containing `.env.docker`.
- Or specify explicitly:
  ```powershell
  docker compose --env-file .env.docker -f docker-compose.ghcr.yml up -d --force-recreate
  ```

### `/actuator/health` redirects to `/login` (HTTP 302)
This usually means actuator endpoints are **protected by Spring Security**. It's not a “not running” issue.
Use `/` and `/swagger-ui/index.html` for basic checks unless you configured actuator to be public.

---

## 6) URLs (for docs)

- Base URL: `http://localhost:8089/`
- Swagger UI: `http://localhost:8089/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8089/v3/api-docs`
