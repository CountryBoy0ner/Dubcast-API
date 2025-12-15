# cURL Examples — Dubcast API

Base URL:

```bash
BASE_URL="http://localhost:8089"
```

---

## 1. Authentication

### Login

```bash
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin123"}'
```

### Register

```bash
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"User123"}'
```

### Validate token

```bash
curl -s -X POST "$BASE_URL/api/auth/validate" \
  -H "Content-Type: application/json" \
  -d '{"token":"<token>"}'
```

---

## 2. Profile (JWT required)

```bash
TOKEN="<paste_token_here>"
```

### Get current profile

```bash
curl -s "$BASE_URL/api/profile/me" \
  -H "Authorization: Bearer $TOKEN"
```

### Update username

```bash
curl -s -X PUT "$BASE_URL/api/profile/username" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"new_name_123"}'
```

### Update bio

```bash
curl -s -X PUT "$BASE_URL/api/profile/bio" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bio":"Music lover"}'
```

---

## 3. Radio (Public)

### Now playing

```bash
curl -i "$BASE_URL/api/radio/now"
```

### Programming (current/next/previous)

```bash
curl -i "$BASE_URL/api/programming/current"
curl -i "$BASE_URL/api/programming/next"
curl -i "$BASE_URL/api/programming/previous"
```

---

## 4. Chat (Public)

### Last messages (limit)

```bash
curl -i "$BASE_URL/api/chat/messages?limit=50"
```

### Paged history

```bash
curl -i "$BASE_URL/api/chat/messages/page?page=0&size=50"
```

---

## 5. Playlists (Public read)

### Get all playlists

```bash
curl -i "$BASE_URL/api/playlist"
```

### Get playlist by ID

```bash
curl -i "$BASE_URL/api/playlist/1"
```

---

## 6. Parser

### Parse track metadata by URL

```bash
curl -i -X POST "$BASE_URL/api/parser/track" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://soundcloud.com/..."}'
```

### Parse playlist by URL

```bash
curl -i -X POST "$BASE_URL/api/parser/playlist" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://soundcloud.com/..."}'
```

### Get track duration

```bash
curl -i -X POST "$BASE_URL/api/parser/duration" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://soundcloud.com/..."}'
```

---

## 7. Admin endpoints (JWT + ADMIN role)

> These endpoints require an admin token and role `ROLE_ADMIN`.

```bash
ADMIN_TOKEN="<paste_admin_token_here>"
```

### Tracks (admin)

```bash
curl -i "$BASE_URL/api/tracks" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Users (admin)

```bash
curl -i "$BASE_URL/api/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Schedule (admin)

```bash
curl -i "$BASE_URL/api/schedule" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---
