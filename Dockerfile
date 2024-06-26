FROM bellsoft/liberica-openjdk-alpine:21@sha256:b4f3b3f5c31e2935f5e941664e45156284ec14fc5745486291a7c45fbccd253d

RUN apk add --no-cache curl
RUN apk add --no-cache dumb-init

COPY build/libs/*.jar app.jar
COPY java-opts.sh /

RUN chmod +x /java-opts.sh
RUN curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", ". /java-opts.sh && exec java ${JAVA_OPTS} -jar app.jar"]