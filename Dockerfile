FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY target/lifegame-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
