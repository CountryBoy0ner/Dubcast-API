# 2. Technical Implementation

This section describes the **technical architecture** of Dubcast and gives a high-level overview of how the system is implemented.

## Contents

- [Tech Stack](tech-stack.md)
- [Criteria](criteria) — ADR for each evaluation criterion
- [Deployment](deployment.md)

---

## Solution Architecture

### High-Level Architecture

```
┌──────────────────────────────┐        HTTP (UI)         ┌───────────────────────────────┐
│ Web Browser                  │  ─────────────────────▶  │ Angular Frontend (SPA)        │
│ (Listener UI)                │                          │ served as static assets       │
└──────────────────────────────┘                          └────────────────┬──────────────┘
                                                                           │
                                                                           │  HTTP (REST API)
                                                                           │
                                        HTTP (REST Admin API)              ▼   
┌──────────────────────────────┐                           ┌──────────────────────────────┐        External HTTP     ┌──────────────────────────────┐
│ Web Browser or Postman       │  ─────────────────────▶   │ Spring Boot Backend          │  ─────────────────────▶  │ SoundCloud (pages / oEmbed)  │
│ Swagger UI (for Admin)       │                           │ (REST API + Swagger)         │                          │ metadata source              │
└──────────────────────────────┘                           └──────────────────────────────┘                          └──────────────────────────────┘
                                                                           │
                                                                           │ JDBC
                                                                           │
                                                                           ▼
                                                           ┌──────────────────────────────┐
                                                           │ PostgreSQL Database          │
                                                           │ users, tracks, playlists,    │
                                                           │ schedule, chat               │
                                                           └──────────────────────────────┘

```

> Diagram is simplified on purpose. Detailed decisions are described in `02-technical/criteria/`.

### System Components

| Component | Description | Technology                                |
|-----------|-------------|-------------------------------------------|
| **Web UI** | SPA in browser (client-side), served as static assets | Angular                                   |
| **Backend API** | Business logic and REST endpoints (public + authenticated + admin). Also provides OpenAPI/Swagger UI for admin operations. | Java 17, Spring Boot                      |
| **Database** | Persistent storage for users, tracks, playlists, schedule entries, chat messages. Includes constraints/indexes and schedule overlap protection. | PostgreSQL                                |
| **External Service** | Track/playlist metadata is imported from SoundCloud URLs (parsing layer). | SoundCloud pages / oEmbed + parsing logic |
| **Containerization** | Unified local run and demo setup. | Docker + Docker Compose                   |
| **CI/CD** | Automated checks (format/tests), artifact upload, security scans, image publish to GHCR. | GitHub Actions, Trivy                     |

### Data Flow

```
[User opens Radio page]
        ↓
[Web UI  loads]
        ↓
[Frontend requests /api/radio/now and related endpoints]
        ↓
[Backend calculates current track by server time]
        ↓
[Backend reads schedule/track data from PostgreSQL]
        ↓
[API response → UI updates “Now playing” / artwork / status]
```



## Key Technical Decisions

| Decision                                                      | Rationale | Alternatives Considered            |
|---------------------------------------------------------------|-----------|------------------------------------|
| Use **Spring Boot (Java 17)** as a single backend application | Fast development, strong ecosystem (security, validation, OpenAPI), suitable for a diploma scope | Node.js, .NET, Python frameworks   |
| Use **Angular (TypeScript framework)** for UI                 | Modern SPA for interactive UI (chat, profiles), clear separation from backend API | Thymeleaf(template engine)         |
| Store schedule and enforce correctness at DB level            | Prevents overlaps and invalid time ranges even for manual/admin usage | Validate only in application code  |
| Use **Docker Compose** for local run/demo                     | One command to run DB + backend with predictable environment | Manual setup, local DB installation |
| Add **Trivy security scans** in CI/CD                         | Early detection of HIGH/CRITICAL vulnerabilities in repo and container image | Skip scanning, only manual review  |

---

## Security Overview

| Aspect | Implementation |
|--------|----------------|
| **Authentication** | JWT-based login/registration; token validation endpoint |
| **Authorization** | Role-based access control (USER / ADMIN). Admin endpoints restricted |
| **Data Protection** | HTTPS recommended for production; database-level constraints; sensitive values kept out of Git |
| **Input Validation** | Bean Validation + server-side checks; DB constraints for consistency |
| **Secrets Management** | Local `.env` / `.env.docker`; CI uses GitHub Secrets (no secrets committed) |
