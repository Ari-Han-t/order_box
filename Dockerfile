# =========================================================================
# Dockerfile: Multi-stage build for Real-Time Stock Exchange Simulation
# =========================================================================

# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy POM and resolve dependencies for efficient layer caching
COPY pom.xml .
RUN mvn dependency:resolve -B

# Copy source code and build shaded fat jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/stock-exchange-simulation-1.0.0.jar app.jar

ENV PORT=8080 \
    DB_URL=jdbc:mysql://mysql:3306/stock_exchange?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
    DB_USER=root \
    DB_PASSWORD=rootpassword

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
