FROM amazoncorretto:21-alpine-jdk
COPY infra/config-server/target/config-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]