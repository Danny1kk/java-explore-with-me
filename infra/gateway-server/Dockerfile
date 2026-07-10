FROM amazoncorretto:21-alpine-jdk
COPY infra/gateway-server/target/gateway-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]