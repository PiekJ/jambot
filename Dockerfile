#
# Build stage
#
FROM maven:3-openjdk-19-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:19-slim
LABEL org.opencontainers.image.source="https://github.com/PiekJ/jambot"
ENV TZ=Europe/Amsterdam
ENV JAVA_OPTS -server
EXPOSE 8080
RUN apt update \
    && apt install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app
COPY --from=build /home/app/target/*.jar /app/run.jar
WORKDIR /app
ENTRYPOINT java $JAVA_OPTS -jar run.jar
HEALTHCHECK --start-period=10s --interval=10s --timeout=3s --retries=3 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1