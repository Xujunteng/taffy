FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p /app/data

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]