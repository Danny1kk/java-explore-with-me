FROM amazoncorretto:21-alpine-jdk
COPY core/main-service/target/main-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]