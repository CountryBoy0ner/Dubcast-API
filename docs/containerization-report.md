# Отчёт по контейнеризации проекта **Dubcast**

## 1. Обзор

Цель контейнеризации — сделать развёртывание Dubcast воспроизводимым на любой машине с Docker и минимальными ручными действиями.  
Стек контейнеров:

- `servicesite-backend` — Spring Boot backend + WebSocket + Liquibase + Playwright (парсинг SoundCloud плейлистов).
- `servicesite-db` — PostgreSQL 16 (официальный образ, данные в volume).

Все настройки вынесены в переменные окружения и `.env` / `.env.example`.  
Секреты (пароли, токены) не вшиты в образы и не закоммичены в репозиторий.


## 2. Dockerfile backend (с комментариями)

```dockerfile
# --- Build stage: сборка Spring Boot приложения ---
# Используем полноценный JDK + Maven, чтобы собрать jar
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 1) Копируем только pom.xml, чтобы кэшировать зависимости
COPY pom.xml ./
# 2) Скачиваем зависимости заранее (offline mode),
#    чтобы при следующих сборках с тем же pom.xml всё бралось из кэша
RUN mvn -B -q dependency:go-offline

# 3) Копируем исходники
COPY src ./src
# 4) Собираем fat-jar. Тесты на стадии сборки образа отключены,
#    их логичнее гонять в CI, а не при каждом docker build
RUN mvn -B -q -DskipTests clean package spring-boot:repackage


# --- Runtime stage: запуск приложения + поддержка Playwright ---
# Эта база уже содержит:
#  - JDK
#  - node
#  - браузеры, нужные Playwright
# Нам это нужно для ParserScServiceImpl (парсинг плейлистов через Playwright).
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy AS runtime

# Рабочая директория внутри контейнера
WORKDIR /app

# В образе уже есть non-root пользователь `pwuser`.
# Создаём каталог для логов и отдаём права этому пользователю.
USER root
RUN mkdir -p /app/logs && chown -R pwuser:pwuser /app
USER pwuser

# Кладём только собранный jar из builder-стейджа
COPY --from=builder /app/target/*.jar app.jar

# Внутренний порт, который слушает Spring Boot
EXPOSE 8080

# Переменная для JVM-флагов, чтобы их можно было задавать из docker-compose/K8s,
# например: -Xmx512m, -XX:MaxGCPauseMillis=200 и т.п.
ENV JAVA_OPTS=""

# Точка входа: запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```


## 3. docker-compose.yml (с комментариями)

> Примечание: имена сервисов/volume/сетей могут отличаться от финальной версии в репозитории.  
> Важно именно содержание и структура, а не буквальное совпадение названий.

```yaml
version: "3.9" # Опционально, Docker уже не требует, но можно оставить для явности

services:
  # --- PostgreSQL база данных ---
  servicesite-db:
    image: postgres:16-alpine          # Лёгкий официальный образ PostgreSQL
    container_name: servicesite-db
    # Все чувствительные значения (пароли) кладём в .env, а не в compose
    env_file:
      - .env
    environment:
      # Фолбэки на случай отсутствия переменных в .env
      POSTGRES_DB: ${DB_NAME:-dubcast}
      POSTGRES_USER: ${DB_USERNAME:-dubcast}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-change_me}
    volumes:
      # Персистентный volume для данных БД (stateful, но вынесен за пределы контейнера)
      - dubcast_db-data:/var/lib/postgresql/data
    networks:
      - app-net
    healthcheck:
      # Ждём, пока PostgreSQL станет доступен внутри сети
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME:-dubcast} -d ${DB_NAME:-dubcast} || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      # Необязательно для обычного compose, но хорошо демонстрирует понимание
      # управления ресурсами контейнера
      resources:
        limits:
          cpus: "1.0"
          memory: 512M

  # --- Backend: Dubcast Spring Boot + Playwright ---
  servicesite-backend:
    build:
      context: .                       # Корень backend-проекта
      dockerfile: Dockerfile           # Наш Dockerfile с multi-stage и Playwright
    container_name: servicesite-backend
    # Backend стартует только после того, как БД "здоровая"
    depends_on:
      servicesite-db:
        condition: service_healthy
    env_file:
      - .env                           # Тот же .env, чтобы не дублировать переменные
    environment:
      # Порт, на котором слушает Spring Boot внутри контейнера
      SERVER_PORT: 8080
      # JDBC-URL до Postgres по имени сервиса внутри docker-сети
      DB_URL: jdbc:postgresql://servicesite-db:${DB_PORT:-5432}/${DB_NAME:-dubcast}
      DB_USERNAME: ${DB_USERNAME:-dubcast}
      DB_PASSWORD: ${DB_PASSWORD:-change_me}
    ports:
      # Публикуем backend наружу: хост:контейнер
      - "8089:8080"
    networks:
      - app-net
    restart: unless-stopped            # Автоматический рестарт при падении
    healthcheck:
      # Проверяем health endpoint Spring Boot Actuator
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: "1.0"
          memory: 1G

# Изолированная сеть для взаимодействия backend <-> db
networks:
  app-net:
    driver: bridge

# Персистентный volume для данных PostgreSQL
volumes:
  dubcast_db-data:
```


## 4. Диаграмма пайплайна сборки и взаимодействия контейнеров

Можно вставить как ASCII-диаграмму или перерисовать в draw.io / Miro:

```text
          [Разработчик]
                |
                |  docker compose build
                v
   +-----------------------------+
   | Docker daemon               |
   |  1) Сборка образа backend:  |
   |     - builder stage         |
   |     - runtime (Playwright)  |
   |  2) Использование образа:   |
   |     - postgres:16-alpine    |
   +---------------+-------------+
                   |
        docker compose up
                   |
     +-------------+----------------------------+
     |                                          |
+----v--------------------+        +------------v-----------+
|  servicesite-backend    | <----> |   servicesite-db      |
|  (Spring Boot + WS +    |        |   (PostgreSQL 16)     |
|   Liquibase + Playwright)|       |   данные в volume     |
+-------------------------+        +-----------------------+
      |          ^
      | HTTP/WS  |
      v          |
   [Браузер пользователя]
```


## 5. Описание Docker-образов

### 5.1. Образ `dubcast-backend`

- **Назначение:** backend-приложение Dubcast (REST API, WebSocket, real-time радио, интеграция с SoundCloud, парсинг плейлистов через Playwright).
- **Base image:**
    - `maven:3.9-eclipse-temurin-21` — для сборки jar на стадии builder.
    - `mcr.microsoft.com/playwright/java:v1.49.0-jammy` — как runtime:
        - содержит JDK;
        - содержит node + Playwright + браузеры, необходимые `ParserScServiceImpl`.
- **Размер финального образа:**  
  (заполняется после выполнения `docker images dubcast-backend`, например: `~800 MB`).
- **Основные оптимизации:**
    - multi-stage build: в финальный образ попадает только jar + JDK + Playwright, без Maven и исходников;
    - кэширование зависимостей через отдельное копирование `pom.xml` и `mvn dependency:go-offline`;
    - запуск под non-root пользователем `pwuser`;
    - конфигурация только через ENV и `.env`, без хардкода секретов.

### 5.2. Образ `postgres:16-alpine`

- **Назначение:** хранение данных приложения (пользователи, треки, расписание, сообщения чата).
- **Base image:** официальный `postgres:16-alpine` — лёгкий образ на базе Alpine Linux.
- **Размер финального образа:**  
  (берётся из `docker images postgres:16-alpine`, например: `~250 MB`).
- **Оптимизации:**
    - использование минимального Alpine-образа;
    - все данные выведены в volume `dubcast_db-data`.


## 6. Описание контейнеров

### 6.1. Контейнер `servicesite-backend`

- **Роль в системе:**
    - Основная бизнес-логика Dubcast:
        - аутентификация и авторизация (JWT);
        - API для управления плейлистами, треками и расписанием;
        - real-time аналитика и WebSocket-уведомления о текущем треке;
        - интеграция с SoundCloud (оEmbed + API + Playwright-парсер).
- **Порты:**
    - внутренний: `8080` (EXPOSE);
    - внешний: `8089` (проброшен через compose: `8089:8080`).
- **Volumes:**
    - при необходимости можно вынести `/app/logs` в отдельный volume
      (`dubcast_backend_logs:/app/logs`), в базовой версии можно логировать в stdout/stderr.
- **Переменные окружения (основные):**
    - `SERVER_PORT` — порт приложения внутри контейнера;
    - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — параметры подключения к PostgreSQL;
    - `JWT_SECRET` — секрет для подписи JWT (в .env, не в образе);
    - `TIME_STAMP` — часовой пояс радио (например, `Europe/Vilnius`);
    - `SOUNDCLOUD_CLIENT_ID`, `SOUNDCLOUD_API_BASE_URL` — настройки SoundCloud API.
- **Healthcheck стратегия:**
    - Docker healthcheck обращается к `http://localhost:8080/actuator/health`
      и ожидает `"status":"UP"`.
- **Resource limits (из docker-compose):**
    - CPU: до `1.0` vCPU;
    - RAM: до `1G`.


### 6.2. Контейнер `servicesite-db`

- **Роль в системе:**
    - Хранение данных (Liquibase раскатывает схему при старте backend).
- **Порты:**
    - внутренний: `5432`;
    - наружу порт не пробрасывается (можно оставить только внутренний доступ из сети `app-net`).
- **Volumes:**
    - `dubcast_db-data:/var/lib/postgresql/data` — персистентные данные БД.
- **Переменные окружения:**
    - `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — задаются через `.env`.
- **Healthcheck стратегия:**
    - `pg_isready -U $POSTGRES_USER -d $POSTGRES_DB`.
- **Resource limits:**
    - CPU: до `1.0` vCPU;
    - RAM: до `512M`.


## 7. Метрики (шаблоны для заполнения)

### 7.1. Время сборки образов

Команда для измерения времени сборки backend:

```bash
time docker compose build servicesite-backend
```

Пример таблицы для отчёта (значения нужно вписать после замеров):

| Образ             | Команда                                   | Время (сек) |
|-------------------|-------------------------------------------|-------------|
| dubcast-backend   | `docker compose build servicesite-backend`| XX          |
| postgres:16-alpine| официальный образ                         | 0           |


### 7.2. Размеры образов до/после оптимизации

Команда:

```bash
docker images
```

Пример оформления:

| Образ             | До оптимизации | После оптимизации | Комментарий                          |
|-------------------|----------------|-------------------|--------------------------------------|
| dubcast-backend   | ~XXX MB        | ~YYY MB           | multi-stage, без Maven в runtime    |
| postgres:16-alpine| ~ZZZ MB        | неизменен         | официальный базовый образ           |


### 7.3. Время холодного старта системы

Команда для холодного старта:

```bash
time docker compose up --build
```

В логах backend ищем строку вида:

```text
Started DubcastApplication in XX.XXX seconds
```

Пример описания:

- Полный запуск системы (`docker compose up --build`): ~NN секунд.
- Запуск backend после готовности БД (по логам Spring Boot): ~XX секунд.


### 7.4. Потребление ресурсов в runtime

Команда:

```bash
docker stats servicesite-backend servicesite-db
```

Пример таблицы:

| Контейнер          | CPU (среднее) | RAM (среднее) |
|--------------------|---------------|---------------|
| servicesite-backend| ~A%           | ~B MB         |
| servicesite-db     | ~C%           | ~D MB         |

Эти значения можно снять на локальной машине и внести в отчёт.


---

## 8. Как использовать этот отчёт

- Включить файл в репозиторий в каталог `docs/` (например, `docs/containerization-report.md`).
- Сослаться на него из общего README.
- На защите можно:
    - показать Dockerfile и docker-compose с комментариями;
    - открыть схему взаимодействия контейнеров;
    - назвать реальные метрики (время сборки, размеры образов, холодный старт, потребление ресурсов).
