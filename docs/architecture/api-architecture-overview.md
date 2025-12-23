# Dubcast Radio — High-Level API Architecture Overview

This document provides a system context view, request flow overview, and module breakdown for the **current** Dubcast Radio API.

> **Important:** The current API uses the `/api` prefix **without** URL versioning (there is no `/api/v1` path prefix yet).  
> Example base URL in local Docker setup: `http://localhost:8089/api`.

---

## 1. System Context Diagram

```text
┌───────────────────────────┐          ┌──────────────────────────┐
│  Frontend / Admin Panel   │  HTTP    │      Dubcast API         │
│  (Web UI / Tools / Postman├─────────▶│  (Spring Boot, REST)      │
└───────────────────────────┘          ├───────────────┬──────────┘
                                       │               │
                                       │               │
                                       ▼               ▼
                              ┌────────────────┐  ┌──────────────────┐
                              │ Business Logic │  │ SoundCloud Parser │
                              │ Services       │  │ (Playwright)      │
                              └───────┬────────┘  └───────┬──────────┘
                                      │                   │
                                      ▼                   ▼
                              ┌────────────────┐     ┌───────────────┐
                              │ Repositories   │     │ SoundCloud Web │
                              │ (Spring Data)  │     │ pages / embeds │
                              └───────┬────────┘     └───────────────┘
                                      │
                                      ▼
                              ┌────────────────┐
                              │ PostgreSQL     │
                              └────────────────┘
```

---

## 2. Authentication & Security (High-Level)

The API uses **JWT Bearer tokens** for protected endpoints:

- Public endpoints are available without authentication (e.g., `GET /api/radio/now`, `GET /api/programming/current`, chat reads).
- Protected endpoints require header:
  ```
  Authorization: Bearer <accessToken>
  ```
- Admin endpoints require **ROLE_ADMIN**.

Security is enforced by Spring Security + a JWT authentication filter in the `/api/**` security chain.

---

## 3. Request Flow Example

Example endpoint: **POST /api/admin/programming/day/{date}/insert-track**

```text
Client
  │
  ├─► AdminProgrammingController.insertTrackIntoDay(date, trackId, position)
  │
  ├─► RadioProgrammingService.insertTrackIntoDay(...)
  │
  ├─► TrackRepository.findById(trackId)
  │
  ├─► ScheduleRepository.loadDaySchedule(date)
  │
  ├─► Business logic:
  │     - insert track into list
  │     - recalculate order / timestamps
  │     - validate constraints (e.g. position range)
  │
  ├─► ScheduleRepository.saveAll(updatedSlots)
  │
  ▼
Response (200 OK or 4xx on validation/auth errors)
```

---

## 4. API Modules Overview (Matches Current OpenAPI Paths)

### 4.1 Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/validate`

### 4.2 Profile (Authenticated)
- `GET /api/profile/me`
- `PUT /api/profile/username`
- `PUT /api/profile/bio`

### 4.3 Tracks (Admin)
CRUD for track catalog:
- `GET /api/tracks`
- `POST /api/tracks`
- `GET /api/tracks/{id}`
- `PUT /api/tracks/{id}`
- `DELETE /api/tracks/{id}`

### 4.4 Schedule Entries (Admin)
Schedule management endpoints:
- `GET /api/schedule`
- `POST /api/schedule`
- `GET /api/schedule/{id}`
- `PUT /api/schedule/{id}`
- `DELETE /api/schedule/{id}`
- `GET /api/schedule/day?date=YYYY-MM-DD`
- `GET /api/schedule/range?from=...&to=...`

### 4.5 Admin Programming (Admin)
Higher-level schedule operations:
- `GET /api/admin/programming/day?date=YYYY-MM-DD`
- `POST /api/admin/programming/day/{date}/insert-track`
- `PUT /api/admin/programming/day/{date}/reorder`
- `PUT /api/admin/programming/slots/{id}/change-track?trackId=...`
- `DELETE /api/admin/programming/slots/{id}`

### 4.6 Radio (Public)
Current radio status:
- `GET /api/radio/now`

### 4.7 Radio Programming (Public)
Read-only “timeline around now”:
- `GET /api/programming/current`
- `GET /api/programming/previous`
- `GET /api/programming/next`

### 4.8 Chat (Public reads)
- `GET /api/chat/messages?limit=...`
- `GET /api/chat/messages/page?page=...&size=...`

### 4.9 Parser (SoundCloud)
Parsing metadata by URL:
- `POST /api/parser/track`
- `POST /api/parser/playlist`
- `POST /api/parser/duration`

### 4.10 Playlists
- `GET /api/playlist`
- `GET /api/playlist/{id}`
- `DELETE /api/playlist/{id}`
- `POST /api/playlist/import`

---

## 5. Component Responsibilities

| Component | Responsibility |
|---|---|
| Controllers | HTTP layer, request validation, mapping DTOs |
| Services | Business rules, orchestration, scheduling logic |
| Repositories | Database access (PostgreSQL via JPA) |
| Security | JWT auth, role-based access control |
| Parser | SoundCloud page parsing (Playwright) |

---

## 6. Notes & Limitations

- Some responses may return `204 No Content` for “nothing to return” scenarios (e.g., radio now playing / schedule around now).
- Parser endpoints depend on external SoundCloud availability and may fail with 4xx/5xx depending on URL validity and upstream responses.
- URL-based API version prefix (`/api/v1`) is **not implemented yet**; see the Versioning Guide for planned approach.

---

## 7. Summary

This overview provides:

- system context (modules + dependencies)
- request flow example
- module breakdown aligned with current OpenAPI paths
- security/auth context

It satisfies the diploma requirement for a high-level architecture overview.
