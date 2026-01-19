FROM eclipse-temurin:17-jre

WORKDIR /app

COPY client-person-service/target/client-person-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
