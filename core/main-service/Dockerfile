FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY core/pom.xml core/
COPY core/main-service/pom.xml core/main-service/
COPY stats-service/pom.xml stats-service/
COPY stats-service/stats-client/pom.xml stats-service/stats-client/
COPY stats-service/stats-dto/pom.xml stats-service/stats-dto/
COPY stats-service/server/pom.xml stats-service/server/

COPY core/main-service/src ./core/main-service/src
COPY stats-service/stats-client/src ./stats-service/stats-client/src
COPY stats-service/stats-dto/src ./stats-service/stats-dto/src

RUN mvn -f core/main-service/pom.xml clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/core/main-service/target/main-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]