FROM amazoncorretto:17
COPY stats-service/server/target/stats-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]