# =====================================================
# Animed API — imagem da aplicação (não-root)
# Opção 1 da Sprint 3: ACR guarda a imagem / ACI executa
# =====================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM bellsoft/liberica-openjre-alpine:17
WORKDIR /app

RUN apk add --no-cache wget \
    && addgroup -S animed \
    && adduser -S animed -G animed

COPY --from=build /app/target/animed-api.jar app.jar
RUN chown animed:animed app.jar

USER animed

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
