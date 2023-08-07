#
# Package stage
#
FROM eclipse-temurin:11-alpine
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080