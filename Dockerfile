#FROM eclipse-temurin:21-jdk-alpine
FROM openjdk:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]