# Stage 1: Build both modules
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy parent POM and both module POMs first (for layer caching)
COPY pom.xml .
COPY npp-demo/pom.xml npp-demo/
COPY rba-sim/pom.xml rba-sim/

# Copy sources and frontends
COPY npp-demo/src npp-demo/src
COPY npp-demo/frontend npp-demo/frontend
COPY rba-sim/src rba-sim/src
COPY rba-sim/frontend rba-sim/frontend

# VITE_BASENAME is set per module via frontend-maven-plugin environmentVariables in each pom.xml
RUN mvn package -DskipTests

# Stage 2: Tomcat runtime — both WARs in a single container
FROM --platform=linux/amd64 tomcat:10.1-jre21-temurin-jammy

# Remove default ROOT app
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy both WARs
COPY --from=build /app/npp-demo/target/npp-demo-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/npp-simulation.war
COPY --from=build /app/rba-sim/target/rba-sim-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/rba-sim.war

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/npp-simulation/ || exit 1
