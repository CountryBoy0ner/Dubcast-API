# Criterion: Real-time Analytics (Online Listeners Counter)

This criterion documents how Dubcast implements the **real-time “online listeners” counter** using a **heartbeat-based presence** mechanism and **WebSocket (STOMP)** delivery to the UI.

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-07

### Context

Dubcast is a “live radio” product where **social proof** (how many listeners are online) improves UX.  
The system must:

- Update the UI **without page refresh** (near real-time).
- Work for **guests** (no mandatory login) and across SPA route changes.
- Avoid heavy REST polling and reduce load.
- Be simple for a diploma/demo environment (no extra infra like Kafka/Redis required).

Constraints:
- Backend: Spring Boot + STOMP WebSocket
- Frontend: Angular SPA
- Diploma scope (single service instance is acceptable)

### Decision

Implement **real-time analytics** as a heartbeat-driven presence counter:

- Frontend sends **heartbeat events** to **`/app/analytics.heartbeat`** with:
  - `page` (e.g., `/radio`, `/chat`)
  - `listening` (`true/false`)
  - optional `trackId`
- Backend stores listener state **in memory** per WebSocket session (`simpSessionId`) using `ConcurrentHashMap`.
- Backend expires inactive sessions using **TTL = 15 seconds**.
- On each heartbeat the backend **recomputes stats** and **broadcasts** to all clients via:
  - **`/topic/analytics/online`**
- For admin/diagnostics the backend provides:
  - **`GET /api/admin/analytics/online`** → current `OnlineStatsDto`

#### What is considered “real-time” and acceptable delay

**Real-time metric:** `totalOnline` = number of active sessions with `listening=true` and not expired by TTL.  
**Target update latency:** **≤ 5 seconds** (client heartbeat interval), plus WebSocket delivery time.  
Justification: TTL is 15 seconds, so sending every ~5 seconds keeps presence accurate without flooding the server.

### Alternatives Considered

| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| REST polling (`GET /online` every N seconds) | Very simple | Higher load, worse UX, “not live” feeling | WebSocket push already used in project |
| Count only WS connect/disconnect | No periodic heartbeat | Disconnects are unreliable (mobile sleep, network drops) | TTL + heartbeat is more robust for presence |
| Redis/shared store for sessions | Scales across instances | Adds infra/ops complexity | Overkill for diploma/demo |
| Message broker (Kafka/RabbitMQ) for analytics | Scalable event pipeline | Much more setup | Not required by rubric minimum |

### Consequences

**Positive:**
- UI updates instantly via WebSocket push, no page refresh.
- No database writes: analytics is ephemeral and fast.
- Works for guests and authenticated users the same way.

**Negative:**
- In-memory state is **not horizontally scalable** (each backend instance has its own counter).
- Accuracy depends on client timers (background tabs/mobile can throttle heartbeats).

**Neutral:**
- The counter is a **presence approximation**, not an exact streaming-level metric.

---

## Implementation Details

### End-to-end event path (source → BE → channel → UI)

```text
User actions (play/pause + route changes)
  └─ Angular AnalyticsPresenceService (decides listening/page/trackId)
      └─ AnalyticsWsService (SockJS + STOMP publish)
          └─ WS send: /app/analytics.heartbeat  (JSON)
              └─ Backend AnalyticsWsController (ingestion)
                  └─ InMemoryOnlineAnalyticsService (processing + TTL)
                      └─ WS broadcast: /topic/analytics/online (OnlineStatsDto)
                          └─ Angular OnlineListenersComponent subscribes and renders header counter
```

### Data formats

Heartbeat message sent by frontend:

```json
{
  "page": "/radio",
  "listening": true,
  "trackId": 123
}
```

Online stats broadcast from backend:

```json
{
  "totalOnline": 7,
  "generatedAt": "2026-01-07T10:15:30.123+02:00"
}
```

### Backend modules

```
src/main/java/com/Tsimur/Dubcast/analytics/
├── controller/
│   ├── AdminAnalyticsRestController.java     # GET /api/admin/analytics/online
│   └── AnalyticsWsController.java            # STOMP: /app/analytics.heartbeat
├── dto/
│   ├── AnalyticsHeartbeatMessage.java        # page + listening + trackId
│   └── OnlineStatsDto.java                   # totalOnline + generatedAt
└── service/
    ├── OnlineAnalyticsService.java
    └── InMemoryOnlineAnalyticsService.java   # TTL + ConcurrentHashMap
```

### Frontend modules

```
src/app/core/analytics/
├── data-access/analytics-ws.service.ts       # STOMP client subscribe/publish
├── domain/analytics-presence.service.ts      # heartbeat loop (~5s), route + player state tracking
└── models/
    ├── analytics-heartbeat.model.ts
    └── online-stats.model.ts

src/app/shared/online-listeners/              # UI component rendered in header
```

### Processing logic (BE)

- `handleHeartbeat(sessionId, msg)` updates/removes session state:
  - `listening=false` → session removed immediately
  - `listening=true` → updates `lastSeen`, `page`, `trackId`
- `getCurrentStats()`:
  - removes sessions with `lastSeen < now - TTL`
  - counts only `listening=true`

### Delivery and UI update (FE)

- WebSocket connects via SockJS to `/radio-ws`
- Subscribes to `/topic/analytics/online`
- UI binds `stats$` observable and renders `totalOnline` in header

---

## Requirements Compliance Checklist (Rubric Mapping)

### Minimum requirements

| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| 1 | Real event stream in near real-time | ✅ | Heartbeats from real users (`/app/analytics.heartbeat`) |
| 2 | End-to-end event path documented | ✅ | Diagram + description in “End-to-end event path” section |
| 3 | Target delay defined (1–5s) with justification | ✅ | Heartbeat every ~5s, TTL 15s (keeps accuracy) |
| 4 | Data source and format defined | ✅ | JSON heartbeat + JSON stats examples |
| 5 | BE ingestion implemented (WS/SSE/long-poll) | ✅ | STOMP WebSocket `@MessageMapping("/analytics.heartbeat")` |
| 6 | BE processes events as they arrive (aggregation) | ✅ | TTL cleanup + counting in `InMemoryOnlineAnalyticsService` |
| 7 | Endpoint to fetch current state exists | ✅ | `GET /api/admin/analytics/online` |
| 8 | FE visualization updates without reload | ✅ | STOMP subscribe to `/topic/analytics/online` |
| 9 | FE shows message when data is unavailable | ⚠️ | Auto-reconnect exists (`reconnectDelay: 5000`), but explicit UI banner/message may be added (see limitations) |

Legend: ✅ implemented, ⚠️ partially, ❌ not implemented

### Maximum requirements (optional, for 10/10)

| Topic | Status | Notes |
|---|---|---|
| Layered architecture: ingestion/processing/serving described | ✅ | Mapped in sections above |
| Scalability patterns (multi-instance) | ⚠️ | Documented as limitation; would require Redis/shared store |
| Security of WS stream (auth/roles) | ⚠️ | Current design works for guests; can be restricted in prod |
| More advanced aggregates (per track, windows) | ⚠️ | `trackId` exists but aggregation per track is not enabled (dto field is deprecated) |

---

## Known Limitations

| Limitation | Impact | Potential Solution |
|---|---|---|
| In-memory sessions (single instance) | Counters differ per instance when scaling | Move session state to Redis (shared) or use broker relay |
| Client timers can throttle in background | Under-count in mobile/background | Increase TTL or add server-side connect/disconnect heuristics |
| No explicit “analytics unavailable” UI banner | UX unclear during WS outage | Expose connection state to UI and show status indicator |
| No rate limiting for heartbeats | Potential abuse if exposed publicly | Add server-side throttling per session/IP |

---

## References (Code)

Backend:
- `com.Tsimur.Dubcast.analytics.controller.AdminAnalyticsRestController`
- `com.Tsimur.Dubcast.analytics.controller.AnalyticsWsController`
- `com.Tsimur.Dubcast.analytics.service.InMemoryOnlineAnalyticsService`

Frontend:
- `core/analytics/data-access/analytics-ws.service.ts`
- `core/analytics/domain/analytics-presence.service.ts`
- `shared/online-listeners/*`
