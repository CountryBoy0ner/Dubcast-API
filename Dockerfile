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
