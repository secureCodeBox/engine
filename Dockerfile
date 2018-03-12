FROM openjdk:8-jdk-alpine

COPY ./scb-engine/target/engine-0.0.1-SNAPSHOT.jar /scb-engine/app.jar
COPY ./scb-scanprocesses/nmap-process/target/nmap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY ./scb-scanprocesses/test-process/target/test-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/

WORKDIR /scb-engine

EXPOSE 8080

ENTRYPOINT ["java", "-Dloader.path=./lib/", "-jar", "app.jar"]