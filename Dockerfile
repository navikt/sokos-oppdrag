FROM bellsoft/liberica-openjdk-alpine:21.0.7@sha256:143e4c24da2872fad6803fcbde5d335bbbe47dddb8ebf20fd916c02bdc7f463b

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