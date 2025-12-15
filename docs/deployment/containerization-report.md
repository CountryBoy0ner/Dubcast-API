# Containerization Report — Dubcast

## 1. Overview

The goal of containerization is to make **Dubcast** deployment reproducible on any machine
using Docker with minimal manual steps.

The container stack:

- **`servicesite-backend`** — Spring Boot backend (REST API) + Liquibase + Playwright runtime (SoundCloud parsing).
- **`servicesite-db`** — PostgreSQL 16 (official image, state stored in a Docker volume).

Configuration is provided through environment variables and `.env` files (for example, `.env.docker`).
Secrets (passwords, JWT secret, tokens) are **not baked into images** and should **not be committed** to VCS.

---

## 2. Backend Dockerfile (explained)

A typical approach is a **multi-stage build**:

1. **Build stage** (Maven + JDK) produces a runnable Spring Boot fat JAR.
2. **Runtime stage** uses a Playwright-enabled Java image, because the project requires browser automation.

Key points to highlight during a defense/demo:

- Multi-stage build keeps runtime image clean (no Maven, no sources).
- Playwright runtime image provides browsers and dependencies needed for parsing.
- Application runs as a non-root user where possible.
- Runtime config is injected via environment variables.

---

## 3. `docker-compose.yml` (example)

> Names may differ in your repository. What matters is the **structure** and the **principles**.

```yaml
version: "3.9" # Optional: modern Docker Compose does not require it, but it is OK to keep.

services:
  servicesite-db:
    image: postgres:16-alpine
    env_file:
      - .env.docker
    environment:
      POSTGRES_DB: ${DB_NAME:-dubcast}
      POSTGRES_USER: ${DB_USERNAME:-dubcast}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-change_me}
    volumes:
      - dubcast_db-data:/var/lib/postgresql/data
    networks:
      - app-net
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U ${DB_USERNAME:-dubcast} -d ${DB_NAME:-dubcast} || exit 1" ]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: "1.0"
          memory: 512M

  servicesite-backend:
    build:
      context: ../architecture
      dockerfile: Dockerfile
    depends_on:
      servicesite-db:
        condition: service_healthy
    env_file:
      - .env.docker
    environment:
      SERVER_PORT: 8080
      DB_URL: jdbc:postgresql://servicesite-db:${DB_PORT:-5432}/${DB_NAME:-dubcast}
      DB_USERNAME: ${DB_USERNAME:-dubcast}
      DB_PASSWORD: ${DB_PASSWORD:-change_me}
    ports:
      - "8089:8080"
    networks:
      - app-net
    restart: unless-stopped
    healthcheck:
      test: [ "CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"' || exit 1" ]
      interval: 15s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: "1.0"
          memory: 1G

networks:
  app-net:
    driver: bridge

volumes:
  dubcast_db-data:
```

---

## 4. Build and Runtime Flow Diagram

```text
          [Developer]
                |
                | docker compose build
                v
   +-----------------------------+
   | Docker daemon               |
   |  1) Build backend image     |
   |     - build stage (Maven)   |
   |     - runtime (Playwright)  |
   |  2) Pull postgres image     |
   +---------------+-------------+
                   |
            docker compose up
                   |
     +-------------+----------------------------+
     |                                          |
+----v--------------------+        +------------v-----------+
|  servicesite-backend    | <----> |   servicesite-db      |
|  (Spring Boot +         |        |   (PostgreSQL 16)     |
|   Liquibase + Playwright)|       |   data in volume      |
+-------------------------+        +-----------------------+
      |
      | HTTP (8089)
      v
  [Client / Browser / Postman]
```

---

## 5. Image Description

### 5.1 `servicesite-backend` image

- **Purpose:** runs Dubcast REST API and all backend business logic.
- **Runtime base:** `mcr.microsoft.com/playwright/java:*`  
  Required for SoundCloud parsing that relies on Playwright browser automation.
- **Optimizations:**
  - multi-stage build (Maven tools do not end up in runtime);
  - dependency caching (copy `pom.xml` first, warm dependencies);
  - runtime config via environment variables;
  - avoid hardcoding secrets.

### 5.2 `postgres:16-alpine`

- **Purpose:** persistent data storage (users, tracks, schedule, chat messages, etc.).
- **Persistence:** Docker volume `dubcast_db-data`.
- **Why alpine:** smaller footprint.

---

## 6. Container Description

### 6.1 `servicesite-backend`

- **Internal port:** `8080`
- **External port:** `8089` (mapped as `8089:8080`)
- **Key env variables:**
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET` (via `.env.docker`)
  - other configuration as required by the application
- **Healthcheck:** `/actuator/health` should return status `UP`
- **Restart policy:** `unless-stopped`

### 6.2 `servicesite-db`

- **Internal port:** `5432`
- **Volume:** `dubcast_db-data`
- **Healthcheck:** `pg_isready`

---

## 7. Metrics (templates for your measurements)

### 7.1 Build time

```bash
time docker compose build servicesite-backend
```

| Image | Command | Time (sec) |
|------|---------|------------|
| servicesite-backend | `docker compose build servicesite-backend` | XX |
| postgres:16-alpine | pulled from registry | 0 |

### 7.2 Image sizes

```bash
docker images
```

| Image | Size |
|------|------|
| servicesite-backend | XXX MB |
| postgres:16-alpine | YYY MB |

### 7.3 Cold start time

```bash
time docker compose up --build
```

In backend logs find:

```text
Started DubcastApplication in XX.XXX seconds
```

### 7.4 Runtime resource usage

```bash
docker stats servicesite-backend servicesite-db
```

| Container | Avg CPU | Avg RAM |
|----------|---------|---------|
| servicesite-backend | A% | B MB |
| servicesite-db | C% | D MB |

---

## 8. How to Use This Report

- Place the file under `docs/` (for example, `docs/containerization-report.md`).
- Reference it from the repository README.
- During defense/demo:
  - show Dockerfile and docker-compose structure;
  - explain why Playwright runtime image is required;
  - present real measurements (build time, size, cold start, runtime stats).
