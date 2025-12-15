# Dubcast-Web-Radio-Platform

---

Java Spring Boot monolith with Thymeleaf, with plans to separate the front-end application. The interface is minimal and adaptive, and user data (passwords and email addresses) is encrypted with AES-256 at the database level. Lightweight real-time statistics on current listeners are supported.

### Project Documentation

https://docs.google.com/document/d/1DG2AewK1P3ZzPaucLeq_eces0xssqQVkP1OyahGJIiU/edit?usp=sharing


### Best Practices for Git Branch Naming
https://docs.google.com/document/d/1ySuJNWIDRcrLnE398XAK7ELGDpLvmFvQfV96Wp4-aXo/edit?usp=sharing

---

## DataBase
https://docs.google.com/document/d/1eEjRvm0ClqGzF0ZA0219kOlPtwJ-_SWOg6EqLVFVVOw/edit?usp=sharing





# Инструкция запуска backend через Docker Compose

## 1. Предусловия

- Установлен Docker и Docker Compose
- В корне backend-проекта лежат файлы:
    - `docker-compose.yml`
    - `Dockerfile`

## 2. Подготовить файл `.env.docker`

Создайте рядом с `docker-compose.yml` файл `.env.docker`, 

## 3. Сборка и запуск контейнеров

Из корня backend-проекта:

```bash
docker compose up --build
# или, если старая версия:
# docker-compose up --build
```

Compose поднимет два сервиса:

- `servicesite-db` — PostgreSQL
- `servicesite-backend` — Spring Boot + Playwright

## 4. Доступ к приложению

После успешного старта:

- Backend API: http://localhost:8089
- Swagger UI: http://localhost:8089/swagger-ui.html
- Healthcheck (Actuator): http://localhost:8089/actuator/health

## 5. Остановка

```bash
docker compose down
# чтобы вместе с контейнерами удалить данные БД:
# docker compose down -v
```



# ENV переменные

Все настройки берутся из файлов `.env.docker` (для Docker) и `.env.local` (для локального запуска через IDE).

**База данных**

- `DB_NAME` – имя базы данных (по умолчанию `dubcast`)
- `DB_USERNAME` – пользователь БД
- `DB_PASSWORD` – пароль пользователя БД
- `DB_PORT` – порт PostgreSQL (по умолчанию `5432`)

**Backend / Spring**

- `SERVER_PORT` – порт Spring Boot внутри контейнера (по умолчанию `8080`, наружу проброшен на `8089`)
- `JWT_SECRET` – секрет для подписи JWT-токенов (обязательно переопределить вне dev)
- `TIME_STAMP` – часовой пояс радио, например `Europe/Vilnius`

**SoundCloud**

- `SOUNDCLOUD_CLIENT_ID` – актуальный Client ID для SoundCloud API
- `SOUNDCLOUD_API_BASE_URL` – базовый URL API (по умолчанию `https://api-v2.soundcloud.com`)
