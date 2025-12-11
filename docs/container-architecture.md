# Контейнеризация Dubcast

## Образы

### Backend (dubcast-backend)
- Base image: `eclipse-temurin:21-jre-alpine`
- Размер ~XXX MB (по `docker images`)
- Multi-stage: maven → jre
- Запуск под non-root пользователем `app`
- Логи в stdout в формате JSON

### Database (postgres:16-alpine)
- Официальный образ
- Volume для данных: `dubcast_db-data:/var/lib/postgresql/data`

### Frontend (dubcast-frontend)
- Base: `nginx:alpine`
- Статическая раздача собранного фронтенда

## Контейнеры

### servicesite-backend
- Порты: `8080` (внутренний), проброшен на `8089` с хоста
- ENV:
    - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
    - `JWT_SECRET`, `TIME_STAMP`, `SOUNDCLOUD_CLIENT_ID` ...
- Healthcheck: `/actuator/health`
- Restart policy: `unless-stopped`
- Resource limits: 1 CPU, 512MB RAM

### servicesite-db
- Порт: `5432`
- Volume: `dubcast_db-data`
- Healthcheck: `pg_isready`

## Запуск

```bash
docker compose up --build
# или
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
