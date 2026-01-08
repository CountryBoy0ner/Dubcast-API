# Criterion: Back-End (Spring Boot API + WebSocket)

This document describes the **Dubcast** backend architecture: REST API, real-time WebSocket/STOMP messaging, security (JWT/RBAC), persistence (PostgreSQL + JPA + Liquibase), and quality gates (tests + coverage).

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-06

### Context
Dubcast is a “live” web radio where all listeners share the same timeline. The backend must:

- Provide a deterministic **Now Playing** state based on server time.
- Support **admin programming** (tracks, playlists, schedule entries) via REST API.
- Prevent invalid schedule operations (e.g., overlaps, invalid time ranges).
- Provide **chat** with message history and real-time delivery.
- Provide **online listeners counter** (real-time).
- Secure private endpoints using **JWT** and roles (USER/ADMIN).
- Expose an OpenAPI/Swagger contract for testing and admin API usage.
- Be reproducible in a diploma/demo environment (Docker + migrations + versioned configuration).

Constraints:
- Diploma scope (single developer).
- Prefer minimal external infrastructure (keep the solution deployable with Docker Compose).
- Configuration/secrets must not be hardcoded in the repository.

### Decision
Implement the backend as a **Spring Boot** application (Java 17) using a classic layered architecture:

- **Controllers** for REST endpoints (`/api/**`) + WebSocket message handlers.
- **Services** as the business logic boundary (schedule rules, “now playing”, chat, parser orchestration).
- **Repositories** (Spring Data JPA) for persistence.
- **DTOs + mappers** to decouple HTTP contracts from JPA entities.
- **WebSocket/STOMP** for push-based real-time updates (chat, now-playing, analytics counter).
- **Security** with stateless **JWT** authentication and role-based authorization (RBAC).
- **Global error handling** with consistent JSON error responses for API clients.
- **Liquibase migrations** as the source of truth for the database schema.

Build execution uses **Maven Wrapper (`./mvnw`)** to ensure the exact Maven version is reproducible in CI and on any developer machine.

### Alternatives Considered

| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| Microservices split (auth/chat/radio) | Independent scaling, isolation | Too much infra/ops for diploma scope | Monolith is simpler and still modular via packages |
| Session-based auth (stateful) | Simple for server-rendered apps | Harder for SPA + WebSocket; scaling issues | JWT is stateless and SPA-friendly |
| Polling-only “real-time” (no WS) | Easy to implement | Worse UX; higher load; not “live” | STOMP/WebSocket fits radio + chat use case |
| Rely only on WS connect/disconnect for presence | No heartbeat required | Disconnect is unreliable (mobile sleep / network) | TTL + heartbeat gives a more robust “presence” metric |

### Consequences
**Positive:**
- Clear separation of concerns (controller/service/repository) improves maintainability and testability.
- Stateless JWT security fits SPA clients and avoids server session storage.
- Push updates (WebSocket/STOMP) reduce polling and improve UX.

**Negative:**
- WebSocket features require a WS-capable reverse proxy in production.
- In-memory broker/analytics are not horizontally scalable without extra infrastructure.

**Neutral:**
- For diploma/demo scale, in-memory components are acceptable; scaling paths are documented.

---

## Implementation Details

### Package / module layout (example)
```text
src/main/java/com/Tsimur/Dubcast/
├── analytics/                 # Online listeners (presence/heartbeat)
├── config/                    # ApiPaths, SecurityConfig, WebSocketConfig, timezone config
├── controller/                # REST controllers + WS handlers
├── dto/                       # request/response DTOs
├── exception/                 # custom exceptions + global handlers
├── mapper/                    # MapStruct mappers (Entity <-> DTO)
├── model/                     # JPA entities
├── radio/                     # Now-playing computation (clock/events)
├── repository/                # Spring Data JPA repositories
├── security/                  # JWT, filters, RBAC helpers
└── service/                   # business services (Auth/Chat/Radio/Programming/Parser/...)
```

### Key technical decisions

| Decision | Rationale |
|---|---|
| Central API route constants (e.g., `ApiPaths`) | Prevents route drift; easier refactor |
| WebSocket/STOMP with `/topic` + `/app` prefixes | Clear separation of broadcasts vs inbound app messages |
| DTO + mapper approach | Keeps HTTP contract stable and isolates entities |
| PostgreSQL + JPA (Hibernate) | Meets “DB + ORM” diploma requirements |
| Liquibase + `ddl-auto=none` | Schema is versioned, deterministic, and reproducible |
| Maven Wrapper (`./mvnw`) | Build works even without system Maven installed |
| JaCoCo coverage gate (>= 70%) | Enforces minimum test coverage for business logic |

---

## Runtime architecture overview

```text
Client (Angular)                 Backend (Spring Boot)                  PostgreSQL
-----------------               -----------------------                ----------
REST (/api/**)  ──────────────▶  Controllers → Services → Repositories ─▶ DB
WebSocket (/radio-ws)  ◀──────▶  STOMP endpoints + brokers
                               ├─ /topic/chat
                               ├─ /topic/now-playing
                               └─ /topic/analytics/online
```

---

## Security model (JWT + RBAC)

- All protected REST endpoints require:
  `Authorization: Bearer <accessToken>`
- Roles are enforced via Spring Security rules (e.g., `ROLE_ADMIN` for admin endpoints).
- API security is stateless: `SessionCreationPolicy.STATELESS`.
- Unauthorized requests return **401**, forbidden requests return **403**.
- Secrets (JWT signing keys, DB credentials, 3rd-party tokens) are injected via environment variables / `.env` files and are not stored in git.

---

## Real-time features (WebSocket/STOMP)

**Transport**
- STOMP endpoint: `/radio-ws` (SockJS enabled for browser compatibility)
- App destinations: `/app/**`
- Broadcast topics: `/topic/**`

**Chat**
- Persist message → broadcast to `/topic/chat`.
- History is available via REST with pagination.

**Now playing**
- Server clock computes current schedule entry and broadcasts updates to `/topic/now-playing` when the current track changes.

**Online listeners counter**
- Clients send heartbeats to `/app/analytics.heartbeat`.
- Backend keeps TTL-based presence state and broadcasts aggregated counter to `/topic/analytics/online`.
- Optional admin REST endpoint provides current stats for diagnostics.

> Target real-time latency (UI refresh expectation): **~1–5 seconds**, driven by client heartbeat interval and server broadcast.

---

## Persistence (DB + ORM)

- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate via Spring Data repositories
- **Migrations:** Liquibase (schema versioned in `db/changelog/**`)
- Entities are stored in `model/` and mapped to DTOs using MapStruct mappers.

---

## Global error handling

- Domain-specific exceptions (e.g., NotFound/BadRequest/Conflict) are mapped to correct HTTP codes (400/401/403/404/409/500).
- API errors are returned in a unified JSON error structure (stable fields: `status`, `message`, `path`, optional `validationErrors`).

---

## Logging

- Backend logs key events (startup, security/auth errors, critical service operations) and exceptions with stack traces.
- Logs are available via container logs (Docker) and CI logs for troubleshooting.

---

## Build, testing, quality gates

- Standard build target:
  - `./mvnw clean verify`
- Unit tests focus on **service layer** business logic (JUnit 5 + Mockito).
- JaCoCo enforces a minimum coverage gate (**>= 70%**) for the service layer as a diploma quality requirement.

---

## Requirements compliance checklist (Diploma — Back-End minimum)

| # | Requirement | Status | Evidence / Notes |
|---:|---|:---:|---|
| 1 | Modern server framework used (Spring / equivalent) | ✅ | Spring Boot backend application |
| 2 | Uses a database for persistent state | ✅ | PostgreSQL (tracks, schedule, users, chat, etc.) |
| 3 | Uses an ORM for DB access | ✅ | Spring Data JPA / Hibernate |
| 4 | Layered architecture (presentation/business/data) | ✅ | controller → service → repository separation |
| 5 | SOLID-aligned design (SRP/DI/interfaces) | ✅ | Services/repositories injected via DI; responsibilities separated by packages |
| 6 | API interface is clearly described | ✅ | OpenAPI/Swagger via springdoc + Markdown docs |
| 7 | Global centralized error handling | ✅ | Global exception handler(s) returning consistent JSON errors |
| 8 | Logging of key events/errors | ✅ | Application logs via Spring logging; visible in Docker/CI logs |
| 9 | Deployable to a “production-like” environment | ✅ | Docker image / Docker Compose deployment (documented in DevOps/Runbook criterion) |
| 10 | Business logic unit tests + >=70% coverage | ✅ | `./mvnw clean verify` runs tests + JaCoCo gate (>=70%) |

**Legend:** ✅ implemented • ⚠️ partial • ❌ not implemented

---

## Known limitations

| Limitation | Impact | Potential mitigation |
|---|---|---|
| Simple in-memory STOMP broker | Not suitable for multiple backend instances | Use broker relay (RabbitMQ/Redis) |
| In-memory analytics presence | Counter differs per instance if scaled | Shared store (Redis) or centralized analytics service |
| WebSocket needs correct proxy config | Realtime can break behind misconfigured reverse proxy | Document WS proxy config for deployment |
| SoundCloud is a 3rd-party dependency | Metadata/policy changes can break parsing | Fallbacks + retries + clear error reporting |

---

## References (project-local)

- `config/`: `ApiPaths`, `SecurityConfig`, `WebSocketConfig`
- `service/`: business logic services
- `repository/`: Spring Data JPA repositories
- Liquibase changelog: `db/changelog/db.changelog-master.yaml`
- CI build command: `./mvnw clean verify`
