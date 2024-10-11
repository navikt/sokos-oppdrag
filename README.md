# sokos-oppdrag

# Innholdsoversikt
* [1. Funksjonelle krav](#1-funksjonelle-krav)
* [2. Utviklingsmiljø](#2-utviklingsmiljø)
* [3. Programvarearkitektur](#3-programvarearkitektur)
* [4. Deployment](#4-deployment)
* [5. Autentisering](#5-autentisering)
* [6. Drift og støtte](#6-drift-og-støtte)
* [7. Swagger](#7-swagger)
* [8. Henvendelser](#8-henvendelser)
---

# 1. Funksjonelle Krav
Applikasjon er en proxy mellom OppdragZ og Utbetalingsportalen (intern arbeidsflate).

# 2. Utviklingsmiljø
### Forutsetninger
* Java 21
* Gradle 8

### Bygge prosjekt
`./gradlew clean build shadowJar`

### Lokal utvikling
Kjør `./setupLocalEnvironment.sh` for å sette opp prosjektet lokalt.
Applikasjonen trenger en db2 lisens fil for å koble til DB2 lokalt. Kontakt en utvikler fra Team MOBY for å få denne filen.

# 3. Programvarearkitektur
Legg ved skissediagram for hvordan arkitekturen er bygget

# 4. Deployment
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[sokos-oppdrag CI / CD](https://github.com/navikt/sokos-oppdrag/actions)

Push/merge til main branch direkte er ikke mulig. Det må opprettes PR og godkjennes før merge til main branch.
Når PR er merged til main branch vil Github Actions bygge og deploye til dev-fss og prod-fss.
Har også mulighet for å deploye manuelt til testmiljø ved å deploye PR.

# 5. Autentisering
Applikasjonen bruker [AzureAD](https://docs.nais.io/security/auth/azure-ad/) autentisering.
Applikasjonen brukes mest av [Utbetalingsportalen](https://github.com/navikt/sokos-utbetalingsportalen) og derfor brukes det OBO-token
som må genereres for å teste mot dev-miljøet.

# 6. Drift og støtte

### Logging

https://logs.adeo.no.

Feilmeldinger og infomeldinger som ikke innheholder sensitive data logges til data view `Applikasjonslogger`.  
Sensetive meldinger logges til data view `Securelogs` [sikker-utvikling/logging](https://sikkerhet.nav.no/docs/sikker-utvikling/logging)).

### Kubectl

For dev-gcp:
```shell script
kubectl config use-context dev-gcp
kubectl get pods -n okonomi | grep sokos-oppdrag
kubectl logs -f sokos-oppdrag-<POD-ID> --namespace okonomi -c sokos-oppdrag
```

For prod-gcp:
```shell script
kubectl config use-context prod-gcp
kubectl get pods -n okonomi | grep sokos-oppdrag
kubectl logs -f sokos-oppdrag-<POD-ID> --namespace okonomi -c sokos-oppdrag
```

### Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. 
Disse finner man konfigurert i [.nais/alerts-dev.yaml](.nais/alerts-dev.yaml) filen og [.nais/alerts-prod.yaml](.nais/alerts-prod.yaml)
Alarmene blir publisert i Slack kanalen #team-moby-alerts-dev og #team-moby-alerts-prod.

### Grafana
- [sokos-oppdrag](https://grafana.nav.cloud.nais.io/d/fds82z8c0pq0wf/sokos-oppdrag?orgId=1)
---

# 7. Swagger

Integration:
- [Prod-fss](https://sokos-oppdrag.intern.nav.no/api/v1/integration/docs)
- [Dev-fss](https://sokos-oppdrag.intern.dev.nav.no/api/v1/integration/docs)
- [Lokalt](http://0.0.0.0:8080/api/v1/integration/docs)

Oppdragsinfo:
- [Prod-fss](https://sokos-oppdrag.intern.nav.no/api/v1/oppdragsinfo/docs)
- [Dev-fss](https://sokos-oppdrag.intern.dev.nav.no/api/v1/oppdragsinfo/docs)
- [Lokalt](http://0.0.0.0:8080/api/v1/oppdragsinfo/docs)

Oppdragsinfo:
- [Prod-fss](https://sokos-oppdrag.intern.nav.no/api/v1/attestasjon/docs)
- [Dev-fss](https://sokos-oppdrag.intern.dev.nav.no/api/v1/attestasjon/docs)
- [Lokalt](http://0.0.0.0:8080/api/v1/oppdragsinfo/docs)

# 8. Henvendelser
   Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på Github.
   Interne henvendelser kan sendes via Slack i kanalen #po-utbetaling

