FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:eae0a7d9b53ce3c6d6cac1f588c450b9d28729f793ecbf5b74f2e391159262b2

COPY build/install/*/lib /lib

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-cp", "/lib/*:/var/run/secrets/db2license/db2jcc_license_cisuz.jar", "no.nav.sokos.oppdrag.ApplicationKt"]