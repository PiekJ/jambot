FROM eclipse-temurin:19
LABEL org.opencontainers.image.source="https://github.com/PiekJ/jambot"
ENV TZ=Europe/Amsterdam
ENV JAVA_OPTS -server
EXPOSE 8080
RUN mkdir -p /app
WORKDIR /app
COPY target/*.jar /app/run.jar
ENTRYPOINT java $JAVA_OPTS -jar run.jar
HEALTHCHECK --start-period=10s --interval=10s --timeout=3s --retries=3 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1