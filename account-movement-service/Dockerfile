FROM openjdk:17-jdk-slim

WORKDIR /app

COPY account-movement-service/target/account-movement-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
