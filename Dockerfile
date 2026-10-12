# =========================================================================
# Production Dockerfile for Real-Time Stock Exchange Backend
# Uses official Eclipse Temurin 21 JRE Alpine image for lightweight runtime
# =========================================================================

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Create non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy pre-built production Fat JAR
COPY target/stock-exchange-simulation-1.0.0.jar app.jar

# Configuration environment variables (Overridable via docker-compose / cloud platform)
ENV PORT=8085 \
    DB_URL=jdbc:mysql://mysql:3306/stock_exchange?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
    DB_USER=root \
    DB_PASSWORD=rootpassword

EXPOSE 8085

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
