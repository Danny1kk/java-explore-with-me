FROM amazoncorretto:21-alpine-jdk
COPY infra/discovery-server/target/discovery-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]