# Changelog — Dubcast API

This file tracks notable API changes over time.

> Version is reflected in OpenAPI `info.version` and (if introduced later) may also be reflected in URL prefixes.
> Current implementation uses `/api/**` without a `/api/v1` prefix, while the public API version is **v1**.

---

## v1.0 (initial public version)

- Implemented JWT authentication (`/api/auth/login`, `/api/auth/register`, `/api/auth/validate`)
- Added profile endpoints (`/api/profile/me`, `/api/profile/username`, `/api/profile/bio`)
- Added public radio endpoints (`/api/radio/now`, `/api/programming/current|next|previous`)
- Added public chat history endpoints (`/api/chat/messages`, `/api/chat/messages/page`)
- Added playlists endpoints (`/api/playlist`, import endpoint if enabled)
- Added parser endpoints (`/api/parser/track`, `/api/parser/playlist`, `/api/parser/duration`)
- Added admin endpoints for users/tracks/schedule/programming (role-based access)

---

## Unreleased

- (add upcoming changes here)

---
