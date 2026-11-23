FROM eclipse-temurin:21-jre-alpine
RUN apk upgrade --no-cache zlib
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
