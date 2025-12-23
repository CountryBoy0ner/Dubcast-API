# Idempotency & Safe Request Practices — Dubcast Radio API

This document explains how **idempotency** and **safe request** principles apply to the **current Dubcast Radio API** (as implemented today).
It helps API consumers build resilient clients (retries, refresh, double-click protection) and satisfies the advanced documentation requirement.

---

## 1. What is Idempotency?

A request is **idempotent** if performing it multiple times results in the **same final server state** as performing it once.

- **Idempotent examples (in Dubcast):**
   - `GET /api/radio/now`
   - `GET /api/programming/current`
   - `PUT /api/profile/username`
   - `DELETE /api/schedule/{id}` *(final state stays “deleted”; repeated calls may return 404)*

- **Non-idempotent examples (in Dubcast):**
   - `POST /api/tracks` *(creates a new track each time)*
   - `POST /api/schedule` *(creates a new schedule entry each time)*
   - `POST /api/admin/programming/day/{date}/insert-track` *(inserts a new slot; repeat inserts again)*

Idempotency matters because clients often retry requests due to:
- unstable network / timeouts
- mobile reconnection
- UI double click
- reverse proxy retries

---

## 2. Safe Methods in Dubcast

### GET — safe and idempotent
Used for reading data. It **must not change** server state.

Examples:
- `GET /api/chat/messages`
- `GET /api/playlist`
- `GET /api/programming/next`

---

## 3. Idempotent Methods

### PUT — idempotent update
A `PUT` request can be sent multiple times with the same body; the final state remains the same.

Examples:
- `PUT /api/profile/username`
- `PUT /api/profile/bio`
- `PUT /api/tracks/{id}`
- `PUT /api/schedule/{id}`

### DELETE — idempotent removal
Deleting a resource multiple times results in the same final state (resource absent).
The second call may return **404 Not Found**, but does not “delete more” than the first.

Examples:
- `DELETE /api/tracks/{id}`
- `DELETE /api/schedule/{id}`
- `DELETE /api/playlist/{id}`
- `DELETE /api/admin/programming/slots/{id}`

---

## 4. Non‑Idempotent Operations

### POST — not idempotent by default
POST is generally used for creation and can cause duplicates if repeated.

Examples:
- `POST /api/tracks`
- `POST /api/users`
- `POST /api/schedule`
- `POST /api/playlist/import`
- `POST /api/parser/track`
- `POST /api/parser/playlist`

Also note:
- Some POST endpoints are “actions” (not simple creation). For example, inserting or appending tracks changes schedule structure and should **not** be retried automatically.

---

## 5. Recommended Safe Practices for Clients

1. **Never auto‑retry POST** unless you know it is safe for a specific endpoint.
2. **Retry GET safely** (with exponential backoff).
3. Prefer **PUT** for updates and user edits (where possible).
4. Treat **409 Conflict** as a normal business outcome (duplicate email/username, slot is currently playing, etc.).
5. If a request fails with a timeout after sending data, the server **may have processed it**. Avoid repeating non-idempotent requests without checking state first.

---

## 6. Optional Future Enhancement: Idempotency Keys (Not Implemented Yet)

The API may later introduce a header like:

```
Idempotency-Key: <uuid>
```

Possible future behavior:
- The server stores the key for a period of time
- Repeated identical requests with the same key return the same response
- Prevents duplicate creations for endpoints like `POST /api/schedule` or `POST /api/tracks`

**Important:** at the moment, Dubcast API does **not** guarantee Idempotency-Key semantics — clients must implement their own deduplication strategy.

---

## 7. Summary

- **GET / PUT / DELETE** are safe to retry (idempotent; DELETE may return 404 on repeat).
- **POST** is generally **not safe** to retry automatically (can create duplicates or repeated side effects).
- For resilience, clients should retry reads, handle conflicts, and avoid blind POST retries.
