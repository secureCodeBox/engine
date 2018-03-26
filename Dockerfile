FROM openjdk:8-jdk as builder

RUN apt-get -qq update
RUN DEBIAN_FRONTEND=noninteractive apt-get -qq install \
      -y --no-install-recommends \
      maven


COPY . .
RUN mvn install


FROM openjdk:8-jre-alpine

COPY --from=builder ./scb-engine/target/engine-0.0.1-SNAPSHOT.jar /scb-engine/app.jar
COPY --from=builder ./scb-scanprocesses/nmap-process/target/nmap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/test-process/target/test-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/

WORKDIR /scb-engine

EXPOSE 8080

ENTRYPOINT ["java", "-Dloader.path=./lib/", "-jar", "app.jar"]
