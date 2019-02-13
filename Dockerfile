FROM maven:3-jdk-8 as builder
COPY . .
RUN mvn clean install -T6 -DskipTests=true -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn


FROM openjdk:8-jre-alpine

ARG COMMIT_ID=unkown
ARG REPOSITORY_URL=unkown
ARG BRANCH=unkown
ARG BUILD_DATE
ARG VERSION

COPY --from=builder ./scb-engine/target/engine-0.0.1-SNAPSHOT.jar /scb-engine/app.jar
COPY --from=builder ./scb-scanprocesses/nikto-process/target/nikto-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/nmap-process/target/nmap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/zap-process/target/zap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/combined-amass-nmap-process/target/combined-amass-nmap-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/combined-nmap-nikto-process/target/combined-nmap-nikto-scanprocess-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/sslyze-process/target/sslyze-process-0.0.1-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/arachni-process/target/arachni-process-1.0-SNAPSHOT.jar /scb-engine/lib/
COPY --from=builder ./scb-scanprocesses/amass-process/target/subdomain-scanner-process-1.0-SNAPSHOT.jar /scb-engine/lib/

COPY --from=builder ./scb-persistenceproviders/elasticsearch-persistenceprovider/target/elasticsearch-persistenceprovider-0.0.1-SNAPSHOT-jar-with-dependencies.jar /scb-engine/lib/
COPY --from=builder ./scb-persistenceproviders/s3-persistenceprovider/target/s3-persistenceprovider-0.0.1-SNAPSHOT-jar-with-dependencies.jar /scb-engine/lib/
COPY --from=builder ./scb-persistenceproviders/defectdojo-persistenceprovider/target/defectdojo-persistenceprovider-0.0.1-SNAPSHOT-jar-with-dependencies.jar /scb-engine/lib/

WORKDIR /scb-engine

COPY dockerfiles/init.sh .
RUN chmod +x ./init.sh

EXPOSE 8080
EXPOSE 8443

RUN apk add --update curl
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 CMD curl --fail http://localhost:8080/status || exit 1

LABEL org.opencontainers.image.title="secureCodeBox Engine" \
    org.opencontainers.image.description="Orchestrating various security scans." \
    org.opencontainers.image.authors="iteratec GmbH" \
    org.opencontainers.image.vendor="iteratec GmbH" \
    org.opencontainers.image.documentation="https://github.com/secureCodeBox/secureCodeBox" \
    org.opencontainers.image.licenses="Apache-2.0" \
    org.opencontainers.image.version=$VERSION \
    org.opencontainers.image.url=$REPOSITORY_URL \
    org.opencontainers.image.source=$REPOSITORY_URL \
    org.opencontainers.image.revision=$COMMIT_ID \
    org.opencontainers.image.created=$BUILD_DATE

ENTRYPOINT ["./init.sh"]
