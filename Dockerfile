# syntax=docker/dockerfile:1

# Multi-arch Dockerfile:
# - локально на Apple Silicon соберётся/запустится arm64
# - для сервера (amd64) собирайте через buildx: --platform=linux/amd64

ARG BUILDPLATFORM
ARG TARGETPLATFORM

# Собираем JAR на build-платформе (обычно это платформа вашей машины)
FROM --platform=$BUILDPLATFORM gradle:7.4-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon

# Финальный образ
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jdk
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
