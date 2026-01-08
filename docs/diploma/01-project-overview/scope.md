# Project Scope

## In Scope ✅

| Feature | Description | Priority |
|---------|-------------|----------|
| Global live radio playback | One shared timeline for all listeners; joining mid-track seeks to the correct second. | Must |
| “Now Playing” + artwork | UI shows current track title/artwork and stays consistent using server time. | Must |
| Human-curated scheduling (API-only) | Admin manages tracks/playlists/schedule via Swagger/REST; supports day/range queries. | Must |
| Overlap protection | DB-level validation prevents overlapping schedule entries; API returns clear error on conflicts. | Must |
| Real-time online counter | Online listeners counter updates during listening (heartbeat + TTL). | Should |
| Community features | Built-in chat (send + history) and user profile (username/bio/links-style info). | Should |
| Reproducible environment | Docker Compose for backend + PostgreSQL, healthchecks, env-based config. | Must |
| CI/CD automation | GitHub Actions: format check, build/tests, artifacts, Trivy scan, publish image to GHCR (main). | Must |
| OpenAPI documentation | Swagger/OpenAPI contract for REST endpoints. | Must |

## Out of Scope ❌

| Feature | Reason | When Possible |
|---------|--------|---------------|
| Admin UI (web panel) | Admin operations are done via API/Swagger to keep scope manageable. | Future phase |
| Recommendation / “smart” playlists | Goal is **human-curated** programming, not ML-generated content. | Future phase / Never |
| Native mobile apps | Diploma scope is web-first; mobile is not fully tested/optimized. | Future phase |
| Full load testing at scale | Performance targets exist, but full load testing is not included in current scope. | Future phase |
| Music licensing / monetization | Not part of educational prototype scope. | Never (for diploma) |
| Full social network features | No followers/DMs/moderation tools beyond basic chat. | Future phase |

## Assumptions

| # | Assumption | Impact if Wrong | Probability |
|---|------------|-----------------|-------------|
| 1 | SoundCloud pages/metadata remain parsable for import flows | Parser/import may break; fallback would be manual track creation | Medium |
| 2 | Demo runs in Docker environment with stable local network | Playback/real-time updates may look unstable during demo | Low |
| 3 | A single curator/admin is enough for demo programming | Multi-admin workflows and moderation are not covered | Low |

## Constraints

Limitations that affect the project:

| Constraint Type | Description | Mitigation |
|-----------------|-------------|------------|
| **Time** | Diploma timeline limits feature depth (no admin UI, no heavy testing). | Focus on core “live radio” + API-first admin + docs. |
| **Budget** | No paid hosting/services required for demo. | Docker Compose local deployment; optional GHCR images. |
| **Technology** | Backend must be Java Spring Boot; DB is PostgreSQL; REST contract required. | Use Spring Boot + JPA/Liquibase + OpenAPI. |
| **Resources** | Single developer (student). | Keep architecture simple; automate via CI/CD. |
| **External** | SoundCloud is a third-party dependency (format/API may change). | Defensive parsing + clear error responses; can import manually if needed. |

## Dependencies

| Dependency | Type | Owner | Status |
|------------|------|-------|--------|
| SoundCloud (track/playlist source) | External | SoundCloud | ⚠️ |
| PostgreSQL | Technical | PostgreSQL community | ✅ |
| Docker / Docker Compose | Technical | Docker | ✅ |
| GitHub Actions + GHCR | External/Platform | GitHub | ✅ |
| Trivy scanner | Technical | Aqua Security | ✅ |
| OpenAPI/Swagger tooling | Technical | OpenAPI ecosystem | ✅ |
