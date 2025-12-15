# Getting Started — Dubcast Radio API

This guide shows the minimum steps to start using the **Dubcast Radio API**: base URL, authentication (JWT),
and a first authenticated request.

---

## 1. Base URL

All REST API requests use the `/api` prefix:

```text
http://localhost:8089/api
```

> If you run the project via Docker Compose, the backend is typically published as `8089` on the host
> (host `8089` → container `8080`). Your setup may differ.

---

## 2. Authentication (JWT)

Most endpoints require a JWT access token in the `Authorization` header:

```text
Authorization: Bearer <accessToken>
```

### 2.1 Login

**POST** `/api/auth/login`

#### Request body

```json
{
  "email": "admin@example.com",
  "password": "Admin123"
}
```

#### Example (cURL)

```bash
curl -X POST "http://localhost:8089/api/auth/login"   -H "Content-Type: application/json"   -d '{"email":"admin@example.com","password":"Admin123"}'
```

#### Successful response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Save the token and reuse it in secured calls.

### 2.2 Register (optional)

**POST** `/api/auth/register`

```json
{
  "email": "user@example.com",
  "password": "User123"
}
```

---

## 3. Your first API calls

### 3.1 Call a public endpoint (no token)

**GET** `/api/radio/now`

```bash
curl -i "http://localhost:8089/api/radio/now"
```

Expected outcomes:
- `200 OK` with the currently playing track info (when something is playing)
- `204 No Content` if nothing is scheduled at the moment

### 3.2 Call an authenticated endpoint

**GET** `/api/profile/me`

```bash
curl -X GET "http://localhost:8089/api/profile/me"   -H "Authorization: Bearer <accessToken>"
```

Example response:

```json
{
  "username": "dubast",
  "bio": "Music lover"
}
```

---

## 4. Roles and access rules (quick overview)

- **Public** endpoints: can be accessed without a token (e.g. `/api/radio/**`, `/api/programming/**`, chat GET endpoints).
- **User** endpoints: require a valid JWT (e.g. `/api/profile/**`).
- **Admin** endpoints: require **ROLE_ADMIN** (e.g. `/api/admin/**`, `/api/users/**`, `/api/tracks/**`, `/api/schedule/**`).

If you call an admin endpoint as a normal user, you should receive **403 Forbidden**.

---

## 5. Common errors

The API uses a unified JSON error format (see `reference/error-handling.md`).

### 401 Unauthorized

Occurs when the token is missing, expired, or invalid.

### 403 Forbidden

Occurs when you are authenticated but lack the required role.

### 400 Bad Request (validation)

Occurs when request body fields fail validation (typically includes `validationErrors`).

---

## 6. OpenAPI / Swagger

Interactive documentation and the generated schema:

- Swagger UI: `/swagger-ui/`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

Example:

```text
http://localhost:8089/swagger-ui/
```

---

## 7. Notes about API versioning

Current public API is treated as **v1**, but endpoints are currently exposed under `/api/...`
(without a `/v1` prefix). See `reference/api-versioning.md` for the lifecycle strategy and how `v2`
would be introduced when breaking changes are needed.
