FROM openjdk:8-jre-alpine
EXPOSE 9999
COPY ./target/uaa-0.0.1-SNAPSHOT.jar uaa-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "uaa-0.0.1-SNAPSHOT.jar"]