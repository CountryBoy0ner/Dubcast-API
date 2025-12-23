# Integration Tutorial — Chat & Radio (Dubcast API)

This tutorial demonstrates a practical workflow for integrating with Dubcast:
- read radio state (public endpoints),
- load chat history (public endpoints),
- authenticate and call a protected endpoint (JWT).

Base URL used in examples:

```
http://localhost:8089
```

---

## 1. Read Radio State (Public)

### 1.1 Now playing

**GET** `/api/radio/now`

- **200** → a track is playing
- **204** → nothing is playing right now

cURL:

```bash
curl -i http://localhost:8089/api/radio/now
```

### 1.2 Current / Next / Previous schedule slots

**GET** `/api/programming/current`  
**GET** `/api/programming/next`  
**GET** `/api/programming/previous`

cURL:

```bash
curl -i http://localhost:8089/api/programming/current
curl -i http://localhost:8089/api/programming/next
curl -i http://localhost:8089/api/programming/previous
```

---

## 2. Load Chat History (Public)

### 2.1 Load last N messages

**GET** `/api/chat/messages?limit=50`

cURL:

```bash
curl -i "http://localhost:8089/api/chat/messages?limit=50"
```

Expected behavior:

- messages in each response are ordered from **oldest to newest** (chronological).

### 2.2 Infinite scroll (paged history)

**GET** `/api/chat/messages/page?page=0&size=50`

Notes:

- `page=0` is the most recent page.
- `size` controls the amount of messages returned.

cURL:

```bash
curl -i "http://localhost:8089/api/chat/messages/page?page=0&size=50"
```

---

## 3. Authenticate and Call a Protected Endpoint

Many endpoints require JWT authentication (for example, `/api/profile/**` and all admin endpoints).

### 3.1 Login

**POST** `/api/auth/login`

```bash
curl -i -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin123"}'
```

Response example:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Save the token.

### 3.2 Call a protected endpoint

**GET** `/api/profile/me`

```bash
TOKEN="paste_token_here"

curl -i http://localhost:8089/api/profile/me \
  -H "Authorization: Bearer $TOKEN"
```

Expected responses:

- **200** → returns profile
- **401** → missing/invalid/expired token
- **403** → authenticated but not permitted (role-based access)

---

## 4. Common Troubleshooting

- **401 Unauthorized**: check the `Authorization: Bearer <token>` header and token expiration.
- **403 Forbidden**: you are logged in, but the endpoint requires a different role (e.g., ADMIN).
- **400 Bad Request**: validate query parameters/body against OpenAPI schema.
- **500 Internal Server Error**: check server logs; this usually indicates an unhandled exception or an external dependency failure.

---
