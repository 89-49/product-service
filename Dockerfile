FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

RUN mkdir -p /app/logs /app/ssl && chown -R appuser:appgroup /app

COPY --chown=appuser:appgroup build/libs/*.jar app.jar
COPY --chown=appuser:appgroup ssl/kafka.server.truststore.jks ssl/kafka.server.truststore.jks

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]