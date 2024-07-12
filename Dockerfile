FROM eclipse-temurin:21
LABEL org.opencontainers.image.title="Jambot"
      org.opencontainers.image.description="Discord bot for playing music in voice channels."
      org.opencontainers.image.url="https://jambot.red"
      org.opencontainers.image.source="https://github.com/PiekJ/jambot"
ENV TZ=Europe/Amsterdam
    JAVA_TOOL_OPTIONS=-server

EXPOSE 8080

RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser && \
    mkdir -p /app && \
    chown -R javauser:javauser /app

WORKDIR /app
USER javauser

COPY target/jambot.jar /app/jambot.jar

ENTRYPOINT java -jar jambot.jar

HEALTHCHECK --start-period=10s --interval=10s --timeout=3s --retries=3 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1