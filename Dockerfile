# =====================================================
# CLYVO VET - Dockerfile multi-stage
# =====================================================

# === Stage 1: Build ===
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia POM e baixa dependências (cache de camadas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia código e empacota
COPY src ./src
RUN mvn clean package -DskipTests -B

# === Stage 2: Runtime ===
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria usuário não-root (requisito DevOps FIAP)
RUN addgroup -S clyvo && adduser -S clyvo -G clyvo

# Copia JAR do stage de build
COPY --from=build /app/target/*.jar app.jar
RUN chown clyvo:clyvo app.jar

USER clyvo

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
