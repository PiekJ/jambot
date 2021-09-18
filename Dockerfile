FROM openjdk:16-slim

ENV JAVA_OPTS -server

RUN mkdir -p /app
COPY target/*.jar /app/run.jar
WORKDIR /app

ENTRYPOINT java $JAVA_OPTS -jar run.jar