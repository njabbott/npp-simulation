# Stage 1: Build
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY frontend ./frontend

# Vite reads this at build time to set asset base paths (e.g. /npp-simulation/assets/...)
ENV VITE_BASENAME=/npp-simulation

RUN mvn package -DskipTests

# Stage 2: Runtime
FROM --platform=linux/amd64 eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/npp-demo-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/npp-simulation/ || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
