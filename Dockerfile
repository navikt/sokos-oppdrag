FROM bellsoft/liberica-openjdk-alpine:21@sha256:c5781987118dcfe21d3b5c4ba9f7ddf572d5fc56da3b1e02842e0b7740c1233d
COPY build/libs/*.jar app.jar
CMD ["dumb-init", "--"]
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java","-jar", "app.jar"]