FROM bellsoft/liberica-openjdk-alpine:21@sha256:45083bf56e4cf13c34c6ec04356deec655638c8aa8443f9dcab7a90fb4db8fb6

RUN apk update && apk add --no-cache \
  dumb-init \
  && rm -rf /var/lib/apt/lists/*

COPY build/libs/*.jar app.jar
COPY java-opts.sh /

RUN chmod +x /java-opts.sh

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", ". /java-opts.sh && exec java ${JAVA_OPTS} -jar app.jar"]