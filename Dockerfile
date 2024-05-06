FROM ghcr.io/navikt/baseimages/temurin:21
COPY app/build/libs/*.jar ./
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"