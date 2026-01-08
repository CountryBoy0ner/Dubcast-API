# Criterion: Front-End (Listener UI)

This document is an **ADR (Architecture Decision Record)** for the Dubcast Listener UI (Angular SPA) and is written to match the diploma rubric (architecture, state management, routing, API integration, error handling, testing).

**Legend for checklist:** ✅ implemented / present, ⚠️ partially or environment-dependent, ❌ missing, 🟡 not verified (needs repo check).

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-06

### Context
Dubcast is a “live radio” web application where listeners share the same moment: everyone hears the same stream and sees the same **Now Playing** information. The Listener UI must support:

- Fast start radio playback.
- **Now Playing**: track title + cover artwork, consistent timing across clients.
- **Real-time features**:
  - Chat (history + live updates)
  - Online listeners counter (live)
- Profile page (bio + links).
- Admin operations are **API-only** (Swagger UI / REST), no dedicated Admin UI.

Constraints:
- Diploma scope → deliver working UI quickly and keep it maintainable.
- Backend integration uses **REST** + **WebSocket (STOMP)**.
- Basic automated tests must exist (unit + e2e smoke).

### Decision
Implement the Listener UI as an **Angular SPA** (TypeScript) using:

- **Angular** for routing, DI, forms, HTTP clients.
- **PrimeNG + PrimeIcons** for UI components.
- **RxJS** for UI state (player, auth/profile, chat, analytics).
- **STOMP + SockJS** for real-time messaging (chat + online counter).
- **SCSS** for theming (dark / blur background from cover artwork).

For local development, use an **Angular proxy** to route `/api` and `/radio-ws` to the backend (avoid CORS and keep reviewer setup simple).

### Alternatives Considered
| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| React SPA | Huge ecosystem | More manual architecture decisions | Angular gives stronger structure for diploma scope |
| Vue SPA | Easy learning curve | Less opinionated for large structure | Angular chosen for strict structure + tooling |
| Server-side templates | Simple deployment | Harder “live” UX | SPA fits reactive/live UI |
| Polling instead of WebSocket | Simple | Worse UX + more traffic | Real-time is core |
| NgRx store | Strong global state | Boilerplate | Services + RxJS are enough for current scope |

### Consequences
**Positive:**
- Clear structure (routes + feature modules + services).
- Real-time UX for chat and online counter.
- Reusable components improve maintainability.

**Negative:**
- SPA bundles are heavier than server-rendered pages.
- WebSocket reconnect/error handling adds complexity.

**Neutral:**
- Admin remains API-only (Swagger UI), not part of Listener UI.

---

## Implementation Details

### Quick Start
```bash
npm install
npm start
```
Default: `http://localhost:4200`

### Backend Proxy (recommended)
Create `proxy.conf.json`:
```json
{
  "/api": {
    "target": "http://localhost:8089",
    "secure": false,
    "changeOrigin": true
  },
  "/radio-ws": {
    "target": "http://localhost:8089",
    "secure": false,
    "changeOrigin": true,
    "ws": true
  }
}
```
Run:
```bash
npm start -- --proxy-config proxy.conf.json
```

### Project Structure (expected)
```text
src/
├── app/
│   ├── core/                 # singleton services (auth, api clients, websocket, player state)
│   ├── shared/               # reusable UI components
│   ├── features/             # feature areas (radio/chat/profile/auth)
│   ├── app.routes.ts         # routing
│   └── app.component.*       # shell layout
├── styles/                   # global SCSS, themes
└── e2e/                      # Playwright tests (or src/e2e depending on setup)
```

### State management
- Global state stored in singleton services using `BehaviorSubject`/`ReplaySubject`.
- Player state lives in a singleton service so playback survives route changes.
- Auth state stored centrally (token + current user profile).

### API integration
- REST is used for initial page data (now playing, message history, profile).
- WebSocket (STOMP) is used for live updates (chat, online counter; optionally “now playing” if backend broadcasts it).

### Error handling (required by rubric)
Expected FE mechanisms:
- HTTP interceptor for:
  - attaching JWT token,
  - centralized 401/403 handling (logout/redirect),
  - mapping backend errors to user-friendly messages.
- UI feedback:
  - toast/snackbar/dialog for errors,
  - loading/skeleton states for async operations.
- WebSocket disconnect handling:
  - show “Real-time unavailable” banner when WS is down,
  - auto-reconnect/backoff (best-effort).

### Testing
Unit tests:
```bash
npm test
# or: ng test --watch=false
```
E2E (Playwright):
```bash
npm run e2e
```

---

## Rubric Compliance Checklist (Front-End)

Legend: ✅ implemented, ⚠️ partially, ❌ not implemented

| # | Requirement (from rubric) | Status | Evidence / How to verify |
|---|---|---|---|
| 1 | SPA on a modern framework (Angular/React/Vue) | ✅ | Check `package.json` for `@angular/*`; run `npm start` |
| 2 | Clear FE architecture (components/pages/services/modules) | ✅ | Inspect folder structure (`core/`, `shared/`, `features/`) |
| 3 | Routing (≥ 3–4 routes) | ✅ | Check routing config; verify `/radio`, `/login`, `/register`, `/profile` |
| 4 | State management (local + global) with justification | ✅ | Look for shared services with `BehaviorSubject`/Signals, etc. |
| 5 | Centralized API layer (typed DTOs/interfaces) | ✅ | Search for API services + TypeScript models; |
| 6 | Real API integration (not static JSON) | ✅ | Run UI + backend, verify live calls in Network tab |
| 7 | Forms + validation (login/register/profile) | ✅ | Check Reactive Forms validators; try invalid inputs |
| 8 | Lists/table UI + at least one filter/search/sort | ❌ | Find a list view (chat history, playlist, schedule) with filter/search/sort |
| 9 | Global error handling (interceptor / handler) | ❌ | Search for `HttpInterceptor` and error mapping; test 401/500 responses |
|10 | User-friendly error messages + loading/skeleton states | ❌ | Check UI for toast/snackbar + loading placeholders |
|11 | Real-time UI updates without refresh (WebSocket) | ✅ | Verify STOMP/SockJS client; disconnect WS and see fallback UI |
|12 | Responsive layout (desktop + mobile) | 🟡 | DevTools device emulation; check key pages |
|13 | Lint/format tools (ESLint/Prettier or equivalent) | ❌ | Check config files; run `npm run lint` / formatting scripts |
|14 | Unit tests for ≥ 3 key components/services | ✅ | Check `*.spec.ts`; run `npm test` and show results |
|15 | E2E/integration tests (1–2 smoke tests) |✅ | Check Playwright/Cypress config; run `npm run e2e` |
|16 | Tokens/secrets not stored in code | ✅ | Ensure no hardcoded JWT tokens; verify storage strategy (localStorage/sessionStorage) and docs |
|17 | Admin UI not required (API-only admin) | ✅ | Architectural decision: admin uses Swagger UI / REST by design |

---


## References
- Angular Router / HttpClient / DI documentation
- PrimeNG / PrimeIcons
- STOMPJS + SockJS
- Playwright
