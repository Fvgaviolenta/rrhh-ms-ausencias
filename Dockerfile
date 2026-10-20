FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Maven Central puede responder transitoriamente durante un build limpio de GitHub Actions.
# Reintentamos la descarga de dependencias antes de fallar la construcción de la imagen.
RUN set -eu; \
    for attempt in 1 2 3; do \
      mvn -q -B -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline && exit 0; \
      echo "No fue posible descargar dependencias (intento ${attempt}/3). Reintentando..."; \
      sleep $((attempt * 5)); \
    done; \
    exit 1
COPY src ./src
RUN mvn -q -B -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 package && \
    JAR="$(ls target/*.jar | head -n 1)" && \
    cp "$JAR" /app/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --no-create-home app
COPY --from=build /app/app.jar app.jar
RUN chown app:app app.jar
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=8085
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
