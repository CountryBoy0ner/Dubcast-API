# Stakeholders & Users

This document describes **who uses Dubcast** and **who is affected by the project**.

---

## Target Audience

| Persona | Description | Key Needs |
|---------|-------------|-----------|
| Listener (Guest/User) | People who want a simple **live radio-like** experience: everyone hears the same track at the same moment. | One-click playback, stable “Now Playing”, live chat, basic profile |
| Community Member / Music Creator | Users who want to participate in the community and share their identity/links (e.g., SoundCloud/YouTube/Instagram). | Profile bio + links, chat history, readable UI, predictable schedule |
| Admin / Curator (API-only) | A person managing tracks, playlists, and schedule via Swagger/REST (no separate admin UI). | CRUD endpoints, validation (no overlaps), clear errors, fast feedback |
| Diploma Reviewer / Supervisor | Evaluators reviewing engineering quality and completeness of criteria. | Clear docs, reproducible setup (Docker), CI pipeline, OpenAPI, simple demo flow |

---

## User Personas

### Persona 1: Listener

| Attribute | Details |
|-----------|---------|
| **Role** | Guest / Registered user |
| **Age** | 16–35 (typical student audience) |
| **Tech Savviness** | Medium |
| **Goals** | Start listening instantly; see what is playing; talk with others in chat |
| **Frustrations** | “On-demand” apps feel isolated; no shared moment; hard to find community |
| **Scenario** | Opens Dubcast, presses Play, sees “Now Playing”, joins chat, sees online counter |

### Persona 2: Community Member / Music Creator

| Attribute | Details |
|-----------|---------|
| **Role** | Listener who wants visibility / networking |
| **Age** | 18–35 |
| **Tech Savviness** | Medium–High |
| **Goals** | Share links to own music; meet new listeners; stay in a niche community |
| **Frustrations** | Sharing links requires external platforms; identity is lost inside music apps |
| **Scenario** | Updates profile bio with links, listens to curated radio, chats, invites friends |

### Persona 3: Admin / Curator (API-only)

| Attribute | Details |
|-----------|---------|
| **Role** | Administrator / curator |
| **Age** | Any |
| **Tech Savviness** | High |
| **Goals** | Create human-curated playlists and schedule them reliably |
| **Frustrations** | Manual scheduling is error-prone; overlaps break playback; no admin UI |
| **Scenario** | Uses Swagger to create tracks/playlists and schedule entries; API/DB rejects overlaps |

---

## Stakeholder Map

### High Influence / High Interest

- **Student (developer):** owns implementation and delivery of criteria
- **Supervisor:** reviews progress, expects clear documentation and results
- **Diploma reviewer/examiner:** validates requirements and demo readiness

### High Influence / Low Interest

- **GitHub / GHCR / Actions:** platform dependency for CI and image publishing
- **Docker / Docker Compose:** runtime platform for reproducible deployment

### Low Influence / High Interest

- **Listeners:** benefit from live radio + chat experience
- **Community/music creators:** benefit from profile links and community engagement

### Low Influence / Low Interest

- **External observers:** people who only view the demo briefly without deep usage
