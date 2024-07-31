FROM bellsoft/liberica-openjdk-alpine:21@sha256:ee40d83d93023b804847568d847e6540799091bd1b61322f8272de2ef369aa8b

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