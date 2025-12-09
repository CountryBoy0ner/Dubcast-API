
# Idempotency & Safe Request Practices — Dubcast Radio API

This document describes how idempotency and safe operation principles apply to
the Dubcast Radio API. It satisfies the advanced requirement for explaining
complex REST design behavior.

---

# 1. What is Idempotency?

A request is *idempotent* if performing it multiple times results in the same
final state as performing it once.

Examples of idempotent operations:

- GET /tracks/42
- DELETE /schedule/10
- PUT /profile/username

Non-idempotent operations:

- POST /tracks
- POST /admin/programming/day/{date}/insert-track

Understanding idempotency helps prevent accidental data corruption caused by:

- network retries  
- UI glitches  
- user double-clicking  
- mobile reconnection events  

---

# 2. Idempotent Methods in Dubcast Radio

### GET — Safe and idempotent
Used for retrieving data, never changes state.

### PUT — Idempotent
You can update a resource multiple times with the same body; the result is identical.

### DELETE — Idempotent
Deleting an already deleted resource returns 404, but calling DELETE twice does not harm state.

---

# 3. Non‑Idempotent Methods

### POST — Not idempotent
Creating track or schedule entries always generates **new entities**.

Example:
```
POST /admin/programming/day/2025-12-08/insert-track
```
Running twice inserts the track twice.

### POST /parser/track
Parsing twice results in multiple operations, even though end DB state may not change.

---

# 4. Recommended Safe Practices for Clients

1. **Avoid retrying POST automatically.**  
2. **Use PUT/PATCH for updates instead of POST where possible.**
3. **Include deduplication keys** (future feature) when sending create requests:
   ```
   Idempotency-Key: <uuid>
   ```
4. **Assume GET is always safe to retry.**
5. **Handle 409 conflicts gracefully** when race conditions occur.

---

# 5. Potential Future Support for Full Idempotency Keys

The API may introduce optional headers:

```
Idempotency-Key: <uuid>
```

Behavior:

- Server logs key
- Repeated request with same key returns the same response
- Prevents duplicated schedule entries or tracks

This aligns with Stripe, PayPal, and AWS best practices.

---

# 6. Summary

Dubcast Radio uses REST principles where:

- GET, PUT, DELETE are idempotent  
- POST is not idempotent  
- Future enhancements may add full idempotency-key support  

This document provides the advanced conceptual explanation required for high‑grade documentation.
