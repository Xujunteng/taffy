FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p /app/data

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]