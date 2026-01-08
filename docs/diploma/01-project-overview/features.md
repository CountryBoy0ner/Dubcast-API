# Features & Requirements

This section describes **what the Dubcast system does** from a user/product perspective.
It follows the diploma template structure (Epics → User Stories → Non‑Functional Requirements).

> Notes about current implementation:
> - **Admin management is API-only** (Swagger / REST). There is **no separate Admin UI**.
> - The backend exposes schedule inspection endpoints (e.g. `GET /api/schedule/day?date=YYYY-MM-DD`, `GET /api/schedule/range?from&to`). They are primarily used for admin/verification and API consumers, **not as a dedicated UI feature**.

---

## Epics Overview

| Epic | Description | Stories | Status |
|------|-------------|---------|--------|
| E1: Public Radio Experience | Listen to a shared radio timeline, see **Now Playing**, use chat and profile features. | 4 | ✅ |
| E2: Programming & Content Management (API-only) | Admin manages tracks/playlists/schedule via REST API and the system validates correctness (no overlaps, valid ranges). | 4 | ✅ |
| E3: Online Listeners Counter | Show the current number of active listeners in the UI. | 1 | ✅ |

---

## User Stories

### Epic 1: Public Radio Experience

The main goal is a “live moment”: all listeners hear the same global timeline, not personalized recommendations.

| ID | User Story | Acceptance Criteria | Priority | Status |
|----|------------|---------------------|----------|--------|
| US-001 | As a guest, I want to open the Radio page and start playback, so that I can listen without registration. | - Player starts/stops successfully<br>- “Now playing” data loads (track title/artwork)<br>- If a user joins in the middle of a track, playback starts at the correct timestamp (seek/sync) | Must | ✅ |
| US-004 | As a user, I want “Now playing” to stay consistent across clients, so that different devices show the same current track. | - Backend defines current track by server time<br>- Minor client clock drift does not break the UI | Should | ✅ |
| US-010 | As an authenticated user, I want to read and send chat messages, so that I can communicate with other listeners. | - UI displays recent chat messages (history)<br>- Sending a message requires authentication (JWT)<br>- Messages are returned in chronological order | Must | ✅ |
| US-011 | As an authenticated user, I want to update my profile (username/bio), so that I can represent myself and share links in my bio. | - API supports updating username and bio<br>- Username uniqueness is enforced<br>- UI reflects updated profile data | Should | ✅ |

### Epic 2: Programming & Content Management (API-only)

Admin operations are performed via Swagger / REST API. The backend focuses on data integrity and safe scheduling.

| ID | User Story | Acceptance Criteria | Priority | Status |
|----|------------|---------------------|----------|--------|
| US-005 | As an admin, I want to CRUD tracks via API, so that I can maintain the catalog used by the schedule. | - Track endpoints are available in Swagger<br>- Validation errors return a clear error response | Must | ✅ |
| US-006 | As an admin, I want to CRUD schedule entries via API, so that radio playback follows the configured timeline. | - Schedule entry endpoints exist in Swagger<br>- `endTime > startTime` validated<br>- Track references must exist | Must | ✅ |
| US-012 | As the system, I want to block schedule overlaps, so that the timeline remains valid and deterministic. | - Database constraint/trigger/function prevents overlapping time intervals<br>- API returns a clear error when an overlap is detected | Must | ✅ |
| US-013 | As an admin, I want programming operations (append/insert/reorder/change track), so that I can adjust the timeline quickly without manual recalculation. | - API provides endpoints for programming operations<br>- Invalid requests are validated (date/ids/position)<br>- Resulting schedule remains consistent | Should | ✅ |

### Epic 3: Online Listeners Counter

A simple online indicator is shown in the UI header.

| ID | User Story | Acceptance Criteria | Priority | Status |
|----|------------|---------------------|----------|--------|
| US-008 | As a listener, I want to see how many people are currently online, so that I can feel the “live” community aspect. | - UI shows a `totalOnline` counter<br>- Counter refreshes without page reload (polling or push) | Must | ✅ |

---

## Use Case Diagram (high-level)

```
                    ┌────────────────────────────────────────────┐
                    │                 Dubcast                    │
                    │                                            │
   ┌───────────┐    │  ┌──────────────────────────────────────┐  │
   │ Listener  │────┼──│  Listen to radio / Now Playing        │  │
   └───────────┘    │  └──────────────────────────────────────┘  │
         │          │  ┌──────────────────────────────────────┐  │
         ├──────────┼──│  Chat (read/send)                    │  │
         │          │  └──────────────────────────────────────┘  │
         │          │  ┌──────────────────────────────────────┐  │
         └──────────┼──│  Profile (username/bio)              │  │
                    │  └──────────────────────────────────────┘  │
                    │  ┌──────────────────────────────────────┐  │
                    │  │  Online listeners counter            │  │
                    │  └──────────────────────────────────────┘  │
   ┌───────────┐    │  ┌──────────────────────────────────────┐  │
   │   Admin   │────┼──│  Manage tracks/playlists/schedule     │  │
   │ (API only)│    │  │  via Swagger / REST                   │  │
   └───────────┘    │  └──────────────────────────────────────┘  │
                    └────────────────────────────────────────────┘
```

---

## Non-Functional Requirements

### Performance

| Requirement | Target | Measurement Method | Status |
|-------------|--------|-------------------|--------|
| Initial page load (Radio) | ≤ 3 seconds on a typical dev machine | Lighthouse / manual measurement | ⚠️ |
| API response time (simple GET endpoints) | ≤ 300 ms average (local Docker) | Manual timing / logs | ⚠️ |
| Concurrent listeners | 50+ (educational target) | Load testing (future) | ⚠️ |

> ⚠️ Performance targets are defined, but full load testing is not part of the current diploma scope.

### Security

- Authentication: JWT-based login (ROLE_USER / ROLE_ADMIN)
- Authorization: role checks in backend endpoints (admin endpoints restricted)
- Input validation: request validation + DB constraints (e.g., time ranges, uniqueness)
- Secrets: stored in `.env` / GitHub Secrets (not committed)

### Accessibility

- Clear focus states and readable typography (basic)
- Further WCAG audit is planned (future improvement)

### Reliability

| Metric | Target | Status |
|--------|--------|--------|
| Uptime (local demo) | “best effort” | ✅ |
| Recovery | restart containers via Docker Compose | ✅ |
| Data integrity | DB constraints + overlap protection | ✅ |

### Compatibility

| Platform/Browser | Minimum Version |
|------------------|-----------------|
| Chrome / Edge | latest stable |
| Firefox | latest stable |
| Mobile browsers | latest stable (not fully tested) |
