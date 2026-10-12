# =========================================================================
# Multi-Stage Production Dockerfile for Real-Time Stock Exchange Backend
# =========================================================================

# --- Stage 1: Build Application with Maven ---
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable Fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Minimal Production Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy shaded Fat JAR from builder stage
COPY --from=builder /build/target/stock-exchange-simulation-1.0.0.jar app.jar

# Cloud environment settings (PORT dynamically injected by Render, Railway, Fly.io, Cloud Run)
ENV PORT=8085 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8085

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
