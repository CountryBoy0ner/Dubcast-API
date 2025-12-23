# API Versioning Guide — Dubcast Radio API

This document describes the versioning strategy used by the **Dubcast Radio API** and how the team will evolve it while keeping client integrations stable.

> **Important:** The API contract is currently **v1**, but the current deployed URLs are **not prefixed** with `/v1`.
> All endpoints live under the base path: `http://localhost:8089/api`.

---

## 1. Versioning Principles

Dubcast follows **semantic API versioning**:

- **Backward compatibility** must be preserved within the same major version (v1).
- **Non-breaking changes** are allowed in v1 (e.g., adding optional fields).
- **Breaking changes** require a new major version (v2, v3…).
- **Deprecated behavior** should remain available for a transition period, with clear documentation.

---

## 2. Current Public Version (v1)

The active public API version is:

- **Contract version:** `v1`
- **Base URL:** `http://localhost:8089/api`
- **OpenAPI metadata:** `info.version = "v1"` (in `openapi.json` / `openapi.yaml`)

This version applies to:

- authentication (`/api/auth/...`)
- profile (`/api/profile/...`)
- radio and programming (`/api/radio/...`, `/api/programming/...`)
- playlists and parser (`/api/playlist...`, `/api/parser/...`)
- admin APIs (`/api/admin/...`, `/api/users/...`, `/api/tracks/...`, `/api/schedule/...`)

---

## 3. What Counts as a Breaking Change?

Breaking changes include:

- renaming or removing endpoints
- renaming or removing fields in request/response models
- changing field types (e.g., `string` → `number`)
- changing HTTP methods (e.g., `PUT` → `PATCH`)
- making an optional field required
- changing response structure (wrapping/unwrapping data)
- changing authentication or authorization rules in a way that breaks clients
- semantic behavior changes (same request now behaves differently)

Non-breaking changes include:

- adding **new optional** fields
- adding new endpoints
- adding optional filters/pagination
- improving error messages (without changing the error schema)

---

## 4. Versioning Strategy

### 4.1 Current strategy: documentation-led v1 (unversioned URL)

Today, the API uses an **unversioned URL** scheme:

- `/api/...` is treated as **v1**.

This approach is acceptable for early versions, but it means breaking changes must be handled carefully
because the URL does not explicitly encode the version.

To keep it safe:

- any breaking change **must not** be released under the same `/api/...` contract
- instead, introduce a separate major version (see below)

### 4.2 Future strategy for v2+: URL-based versioning (recommended)

When a breaking change is required, the recommended approach is to introduce a new prefix, for example:

- **v1 (legacy / current):** `/api/...`
- **v2 (new):** `/api/v2/...`

Example:

```
/api/tracks                 (v1)
/api/v2/tracks              (v2)
```

During a transition period:

- v1 remains available (no breaking changes)
- v2 is released with the new contract
- documentation clearly marks differences and migration steps

> Note: Introducing `/api/v2/...` requires implementation work in the backend routing.
> This document describes the **planned policy**, not a current behavior change.

---

## 5. Deprecation Policy

To ensure a smooth upgrade path:

1. breaking changes are introduced only in new major versions (v2+)
2. v1 remains supported for **at least one full release cycle** after v2 is introduced
3. deprecations are documented in:
   - OpenAPI spec (descriptions / deprecation notes)
   - guides (migration instructions)
   - changelog (see below)

---

## 6. Changelog

A changelog should be maintained at:

- `/docs/reference/changelog.md`

Example entry format:

```md
## v1.1
- Added optional field `durationSeconds` to TrackDto.
- Added query parameter `limit` to chat messages endpoint.
```

---

## 7. Summary

- The current API is **v1** (contract), served under `/api/...`
- v2 is needed **only when** you introduce breaking changes
- best practice for v2+: expose `/api/v2/...` while keeping `/api/...` (v1) during migration
- versioning details should be reflected in OpenAPI and the changelog
