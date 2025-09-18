FROM openjdk:17-jdk-slim

WORKDIR /app

COPY client-person-service/target/client-person-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
