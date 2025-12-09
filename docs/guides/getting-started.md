
# Getting Started — Dubcast Radio API

This guide provides the minimal steps required to start using the Dubcast Radio API.
It explains how to authenticate, how to call your first endpoint, and how to work with
the API securely.

---

# 1. Base URL

All API requests are sent to:

```
http://localhost:8089/api
```

---

# 2. Authentication (JWT)

Before accessing most API endpoints, you must authenticate and obtain a **JWT access token**.

## 2.1 Login

**POST /api/auth/login**

### Request
```json
{
  "email": "admin@example.com",
  "password": "Admin123"
}
```

### cURL
```bash
curl -X POST http://localhost:8089/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"admin@example.com","password":"Admin123"}'
```

### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Save this token — it is required for all secured requests.

---

# 3. Calling Your First Authenticated Endpoint

Most admin and user operations require a header:

```
Authorization: Bearer <token>
```

## Example: Get Current User Profile

**GET /api/profile/me**

### cURL
```bash
curl -X GET http://localhost:8089/api/profile/me   -H "Authorization: Bearer eyJh..."
```

### Successful Response
```json
{
  "username": "dubast",
  "bio": "Music lover"
}
```

---

# 4. Common Authorization Errors

## 401 Unauthorized
Occurs when token is:
- missing
- expired
- malformed

Example:
```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing token"
}
```

## 403 Forbidden
You are authenticated, but do not have the required role (e.g., ADMIN-only endpoint).

```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Access denied"
}
```

---

# 5. Useful Test Flow

1. Register or log in
2. Copy JWT from login response
3. Call authenticated endpoints with:

```
-H "Authorization: Bearer <token>"
```

4. Use API examples from `api-examples.md` to explore additional functionality.

---

# 6. Next Steps

- Read `api-examples.md` for full request/response examples
- Review `api-architecture-overview.md` to understand system structure
- Explore your OpenAPI docs at:
    - `/v3/api-docs`
    - `/v3/api-docs.yaml`  
