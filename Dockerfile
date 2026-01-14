FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./

RUN mvn -B -q dependency:go-offline

COPY src ./src

RUN mvn -B -q -DskipTests clean package spring-boot:repackage

FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy AS runtime

WORKDIR /app

USER root
RUN mkdir -p /app/logs && chown -R pwuser:pwuser /app
USER pwuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
