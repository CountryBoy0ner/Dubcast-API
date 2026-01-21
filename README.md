# Dubcast Backend (Web Radio)

Spring Boot backend for Dubcast, a web radio in the "single broadcast" format. The backend is the single source of truth for broadcast state (Now Playing), schedules, tracks, playlists, chat, likes, and online listeners. Playback happens on the client side via the SoundCloud Widget API.

## Repositories

- Backend: https://github.com/CountryBoy0ner/Dubcast-Web-Radio-Platform
- Frontend: https://github.com/CountryBoy0ner/Dubcast-UI

## Key features

- Single broadcast state: all listeners see the same "Now Playing"
- Schedule and queue based on time intervals
- Tracks and playlists support
- SoundCloud metadata import
  - Track metadata via oEmbed
  - Playlist import via Playwright (dynamic page data)
- Auth with JWT and role based access control (RBAC)
- Real time updates via WebSocket + STOMP
  - Chat
  - Now Playing events
  - Online listeners counter (heartbeat presence)
  - Likes updates
- Database versioning with Liquibase
- OpenAPI and Swagger UI for API exploration (admin functions are HTTP only)

## Tech stack

- Java 17 (LTS)
- Spring Boot, Spring Web, Spring Security
- PostgreSQL
- Liquibase migrations
- WebSocket + STOMP (SockJS compatible)
- Playwright (server side automation for SoundCloud playlist import)
- Docker, Docker Compose
- CI: GitHub Actions, Spotless, JUnit, JaCoCo, Trivy

## Quick start with Docker Compose

### Prerequisites

- Docker and Docker Compose
- Port 8089 must be free on your machine

### Run

1. In the backend project root (where `docker-compose.yml` is located), create `.env.docker`.
2. Use `.env.example` as a template and fill required values.
3. Start:

```bash
docker compose up -d --build
```

This will:
- start PostgreSQL and wait until it is healthy
- build the backend image
- start the backend container

### Verify

- Healthcheck: http://localhost:8089/actuator/health
- Swagger UI: http://localhost:8089/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8089/v3/api-docs

### Stop

```bash
docker compose down
```

Full reset (also removes the database volume):

```bash
docker compose down -v
```

Logs:

```bash
docker compose logs -f servicesite-backend
```

## API documentation

Swagger UI is available when the backend is running:

- http://localhost:8089/swagger-ui/index.html

Use Swagger to:
- inspect endpoints and DTOs
- run admin operations (tracks, playlists, schedules)
- test auth flows (JWT)

## WebSocket (STOMP)

The frontend uses SockJS + STOMP. The common pattern is:

- SockJS endpoint: `/radio-ws` (example)
- Topics: `/topic/...`
- App destinations: `/app/...`

Exact endpoints can differ. Check your WebSocket configuration class and the frontend STOMP client setup.

## Database and migrations

- Liquibase manages schema changes.
- On startup, migrations apply automatically in most setups.
- For a clean state during demos, use `docker compose down -v` to reset the DB volume.
- For convenient testing of application performance, track data with a duration of 10 seconds has been added to the v2.5 migration so that you can easily test that the main feature is working correctly.

## Tests and coverage

Run backend tests:

```bash
.\mvnw.cmd test
```

Run full verification (tests + coverage checks, depends on your build):

```bash
.\mvnw.cmd verify

```

JaCoCo reports are typically generated under `target/site/jacoco/`.

## Code style and quality gates

Check styles

```bash
.\mvnw.cmd -B -ntp spotless:check
```
Apply styles

```bash
.\mvnw.cmd -B -ntp spotless:apply
```

- Spotless enforces formatting in CI.
- Trivy scans the repo and the container image in CI.
- If CI fails on formatting, run your formatter task locally and commit the changes.

## Troubleshooting

### Port 8089 is in use

Stop the process using it or change the port mapping in `docker-compose.yml`.

### Database does not become healthy

- Check logs: `docker compose logs -f servicesite-db`
- Ensure no other PostgreSQL instance uses the same port
- If you want a clean start, run: `docker compose down -v`

### Swagger is not reachable

- Check health endpoint first: `/actuator/health`
- Inspect backend logs: `docker compose logs -f servicesite-backend`

## License

Educational project. Add a license file if you plan to publish under a specific license.
ь