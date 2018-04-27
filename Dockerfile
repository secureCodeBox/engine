FROM maven as builder
COPY . .
RUN mvn clean install -DskipTests=true -B


FROM openjdk:8-jre-alpine

COPY --from=builder ./scb-engine/target/engine-0.0.1-SNAPSHOT.jar /scb-engine/app.jar
COPY --from=builder ./scb-scanprocesses/nikto-process/target/nikto-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/nmap-process/target/nmap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/test-process/target/test-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/zap-process/target/zap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-persistenceproviders/elasticsearch-persistenceprovider/target/elasticsearch-persistenceprovider-0.0.1-SNAPSHOT-jar-with-dependencies.jar /scb-engine/lib/

WORKDIR /scb-engine

EXPOSE 8080

ENTRYPOINT ["java", "-Dloader.path=./lib/,./plugins/", "-jar", "app.jar"]
