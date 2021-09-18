#
# Build stage
#
FROM maven:3-openjdk-16-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:16-slim
ENV JAVA_OPTS -server
RUN mkdir -p /app
COPY --from=build /home/app/target/*.jar /app/run.jar
WORKDIR /app
ENTRYPOINT java $JAVA_OPTS -jar run.jar