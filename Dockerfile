FROM bellsoft/liberica-openjdk-alpine:21@sha256:b4f3b3f5c31e2935f5e941664e45156284ec14fc5745486291a7c45fbccd253d
COPY build/libs/*.jar app.jar
CMD ["dumb-init", "--"]
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java","-jar", "app.jar"]