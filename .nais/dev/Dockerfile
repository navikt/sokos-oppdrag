FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21-dev@sha256:716549c98e2e48219c05002f62e94395404787f65c1d07ae39f5f5de8c009eaf

COPY build/install/*/lib /lib

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-cp", "/lib/*:/var/run/secrets/db2license/db2jcc_license_cisuz.jar", "no.nav.sokos.oppdrag.ApplicationKt"]