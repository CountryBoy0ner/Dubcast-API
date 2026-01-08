# FAQ & Troubleshooting (Dubcast)

Below are short answers to common questions and typical diagnostics when running Dubcast (Backend + Frontend).

---

## How to run the project (locally)

### 1) Run the Back-End (Spring Boot + PostgreSQL) via Docker Compose

**Prerequisites:** Docker + Docker Compose installed.

1. Go to the **backend** folder (where `docker-compose.yml` is located).
2. Make sure you have **`.env.docker`** (it is loaded via `env_file:` in compose). At minimum it usually contains:
    - `DB_NAME`
    - `DB_USERNAME`
    - `DB_PASSWORD`
    - (optional) `JWT_SECRET`, `RADIO_TIMEZONE`, etc. — if your app uses them

3. Start the stack:

```bash
docker compose up --build
```

How to verify it’s running:
- Health: `http://localhost:8089/actuator/health`
- Swagger UI: `http://localhost:8089/swagger-ui/index.html`
- OpenAPI: `http://localhost:8089/v3/api-docs`

Stop:
```bash
docker compose down
```

Full DB cleanup (warning: removes the volume):
```bash
docker compose down -v
```

---

### 2) Run the Front-End (Angular SPA)

**Prerequisites:** Node.js (LTS), npm.

1. Go to the **frontend** folder (`dubcast-ui`).
2. Install dependencies:
```bash
npm install
```

3. To call the backend **without CORS issues**, the simplest approach is to use an **Angular proxy**.  
   Create **`proxy.conf.json`** in the frontend root (next to `package.json`):

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

4. Start the frontend with the proxy:
```bash
npm start -- --proxy-config proxy.conf.json
```

Frontend URL: `http://localhost:4200`  
Requests to `/api/**` and WebSocket `/radio-ws` will be proxied to the backend.

> If you don’t want to use a proxy, you must configure the base URL in `environment.ts` and enable CORS on the backend. For a diploma demo, proxy is usually simpler and more stable.

---

## FAQ (frequently asked questions)

### General

**Q: What is Dubcast and how does it work?**  
A: It’s a web radio app: the server determines “what is playing now” based on schedule and time, while the client shows Now Playing and plays the track.

**Q: Do I need to register to listen to the radio?**  
A: No, you can listen as a guest. An account is required for chat and profile features.

**Q: Why is “Now Playing” the same for everyone?**  
A: Because the current track is computed on the server (a single shared timeline for all users).

---

### Account & Access

**Q: I can’t log in — what should I check first?**  
A: Check your credentials and confirm the backend is reachable (e.g., Swagger UI opens or `/actuator/health` returns OK). If the API is down, login won’t work.

**Q: After login I still see myself as a “guest”. Why?**  
A: Usually the JWT wasn’t stored or has expired. Refresh the page and log in again. Very strict privacy settings/extensions can also interfere with token storage.

---

### Features

**Q: Chat doesn’t update in real time.**  
A: Most likely WebSocket didn’t connect. Check that the backend is running, `/radio-ws` is reachable, and `"ws": true` is set in `proxy.conf.json`.

**Q: “Online listeners” counter doesn’t update.**  
A: It updates via WebSocket + heartbeat. If the WS connection is not established or the heartbeat is not being sent, the value can “freeze”.

**Q: Why don’t I see track cover art / metadata?**  
A: Possible reasons:
- the current track **does not have artwork** at the source;
- the source (SoundCloud) temporarily doesn’t return metadata or changed its page/API behavior;
- an import/parsing error on the backend (check backend logs).

---

## Troubleshooting (common problems)

### Common Issues

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| Page won’t load / API doesn’t respond | Backend is not running or the port/URL is incorrect | Check `docker compose ps`, open `/actuator/health`, verify port mapping (usually `8089:8080`) |
| Login doesn’t work | Wrong credentials or API is unavailable | Try `/api/auth/login` in Swagger; confirm backend is running |
| Chat doesn’t work / messages don’t arrive | WebSocket can’t connect | Check `proxy.conf.json` (`ws: true`), path `/radio-ws`, browser console and backend logs |
| Now Playing doesn’t update | No schedule entries / DB or migrations issue | Check Liquibase migrations, ensure schedule exists, check backend logs |
| After Docker restart the database is “empty” | The volume isn’t used or was removed | Check `docker volume ls`, confirm the volume is declared in compose; don’t use `down -v` if you want to keep data |
| Playlist/track import fails in Docker | Playwright/dependencies not available in the container, or the external source blocks access | Check backend logs; ensure runtime image includes Playwright (and browsers are present in the base image) |

---

### Error Messages (API)

| Error Code/Message | Meaning | How to Fix |
|-------------------|---------|------------|
| `401 Unauthorized` | Not authenticated or token expired | Log in again (get a new JWT), verify the Authorization header |
| `403 Forbidden` | Not enough permissions (e.g., admin endpoint) | Use an ADMIN user or call an allowed endpoint |
| `404 Not Found` | Resource not found | Verify URL/parameters/ID/shortCode |
| `409 Conflict` | State conflict (e.g., schedule overlap) | Fix the data (time/conditions), retry the request |
| `5xx Server Error` | Server-side failure | Check backend logs, verify DB and dependencies |

---

## Getting Help

### Self-Service
- Project documentation: `docs/`
- Swagger UI (backend): `/swagger-ui/index.html`

### Reporting Bugs

When creating a bug report (Issue), include:
1. Steps to reproduce
2. Expected behavior
3. Actual behavior
4. Screenshots (if applicable)
5. Browser/OS version and logs (console + backend)
