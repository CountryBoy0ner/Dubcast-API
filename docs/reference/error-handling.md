
# Error Handling — Dubcast Radio API

This document describes the unified error response format used by the Dubcast Radio API.
Understanding these error structures helps developers diagnose issues and implement robust
client-side logic.

---

# 1. Error Response Structure

Every error follows the same JSON format:

```json
{
  "timestamp": "2025-12-07T12:45:33Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid parameters",
  "path": "/api/admin/programming/day/2025-12-08/insert-track",
  "validationErrors": {
    "position": "must be >= 0"
  }
}
```

### Field Breakdown

| Field | Meaning |
|-------|---------|
| `timestamp` | When the error occurred |
| `status` | HTTP status code |
| `error` | Short error label |
| `message` | Human-readable explanation |
| `path` | Endpoint where the error occurred |
| `validationErrors` | Optional map of field → validation message |

---

# 2. Common Error Types and Examples

## 400 — Bad Request
Returned when input is invalid.

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid position value"
}
```

---

## 401 — Unauthorized
Returned when no valid JWT token is provided.

```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing token"
}
```

---

## 403 — Forbidden
Returned when the user is authenticated but lacks permissions (e.g., not ADMIN).

```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Access denied"
}
```

---

## 404 — Not Found
Returned when a resource does not exist.

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Track not found: 42"
}
```

---

## 409 — Conflict
Returned when trying to use non-unique fields or violate constraints.

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Username already taken"
}
```

---

# 3. Validation Errors

When Spring validation fails, the API returns:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "validationErrors": {
    "email": "must be a valid email",
    "password": "must not be blank"
  }
}
```

---

# 4. Server Errors

Unexpected server issues return 500:

```json
{
  "status": 500,
  "error": "INTERNAL_SERVER_ERROR",
  "message": "Unexpected error occurred"
}
```

---

# 5. Summary

The error format is:

- consistent
- predictable
- safe for machine parsing
- aligned with REST industry practices

This completes the required **Error Handling Documentation** for the diploma.
