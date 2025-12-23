# Error Handling — Dubcast Radio API

This document describes how **Dubcast Radio API** returns errors for `/api/**` endpoints.

> Note: Most API errors are returned in a unified JSON format (`ErrorResponse`).  
> Some *framework-level* failures (for example, malformed JSON or type conversion errors) may still be returned by Spring in its default error format unless a dedicated handler is added.

---

## 1. Unified Error Response Format

When the API returns an error body, it follows this structure:

```json
{
  "timestamp": "2025-12-15T12:45:33.123+02:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/profile/username",
  "validationErrors": {
    "username": "must match \"^[A-Za-z0-9_.]+$\""
  }
}
```

### Field breakdown

| Field | Type | Meaning |
|---|---|---|
| `timestamp` | string (date-time) | When the error occurred (ISO-8601 with offset) |
| `status` | number | HTTP status code |
| `error` | string | HTTP reason phrase (e.g., `"Bad Request"`, `"Not Found"`, `"Conflict"`) |
| `message` | string | Human-readable explanation |
| `path` | string | Request URI that produced the error |
| `validationErrors` | object (optional) | Map of `field -> validation message` (present for validation errors) |

---

## 2. HTTP Status Codes Used

### 400 — Bad Request

Returned when request data is invalid.

**Typical causes**
- Bean Validation failed for a request body (`@Valid`) → `MethodArgumentNotValidException`
- Invalid inputs detected by controller/service logic (when mapped to 400)

**Body**
- Always contains `validationErrors` for bean validation failures.

Example (validation failure):

```json
{
  "timestamp": "2025-12-15T12:45:33.123+02:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/profile/username",
  "validationErrors": {
    "username": "size must be between 3 and 50"
  }
}
```

---

### 401 — Unauthorized

Returned when the request requires authentication, but the JWT token is missing/invalid/expired.

**Typical causes**
- No `Authorization: Bearer <token>` header
- Invalid signature / expired token

Example:

```json
{
  "timestamp": "2025-12-15T12:50:01.000+02:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/profile/me"
}
```

> The exact `message` depends on the security entry point configuration. Clients should rely on the **HTTP status** first.

---

### 403 — Forbidden

Returned when the user is authenticated, but does not have enough permissions.

**Typical causes**
- Calling ADMIN-only endpoints with a non-admin token

Example:

```json
{
  "timestamp": "2025-12-15T12:51:10.000+02:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/users"
}
```

---

### 404 — Not Found

Returned when the requested resource does not exist, or the route is unknown.

**Typical causes**
- Application-level “not found” (`NotFoundException`)
- Persistence-level not found (`EntityNotFoundException`)
- Unknown path (when `NoHandlerFoundException` is enabled)

Example:

```json
{
  "timestamp": "2025-12-15T13:00:00.000+02:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/tracks/999999"
}
```

---

### 409 — Conflict

Returned when the request conflicts with current server state or uniqueness constraints.

**Typical causes**
- Email already used (duplicate user registration / user creation)
- Slot is currently playing and cannot be modified (business rule conflict)

Example (duplicate email):

```json
{
  "timestamp": "2025-12-15T13:05:00.000+02:00",
  "status": 409,
  "error": "Conflict",
  "message": "User with this email already exists",
  "path": "/api/auth/register"
}
```

Example (slot is currently playing):

```json
{
  "timestamp": "2025-12-15T13:06:10.000+02:00",
  "status": 409,
  "error": "Conflict",
  "message": "Slot is currently playing",
  "path": "/api/admin/programming/slots/123"
}
```

---

### 500 — Internal Server Error

Returned for unexpected/unhandled exceptions.

Example:

```json
{
  "timestamp": "2025-12-15T13:10:00.000+02:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Internal server error",
  "path": "/api/parser/playlist"
}
```

**Important notes**
- The server logs the full stack trace.
- Clients should treat 500 as retryable only when they can safely retry (see idempotency rules).

---

## 3. Client Recommendations

- Always branch client logic primarily by **HTTP status code**.
- For `400` validation errors, read `validationErrors` and show field-level messages.
- For `401`, refresh/re-login and retry only after obtaining a valid token.
- For `403`, do not retry automatically; show “insufficient permissions”.
- For `409`, treat as a business rule / uniqueness conflict and prompt the user to resolve it.
- For `500`, consider a safe retry only for **idempotent** operations (GET/PUT/DELETE).

---

## 4. Summary

Dubcast Radio API aims to provide **predictable, machine-readable** error responses.
When present, errors follow the `ErrorResponse` schema with:

- stable `status`, `error`, `message`, `path`
- optional `validationErrors` for request validation issues
