# sokos-oppdrag

Kan brukes som utgangspunkt for å opprette nye Ktor-apper for Team Motta og Beregne.

## Tilpass repo-et
1. Gi rettighet for å kjøre scriptet `chmod 755 setupTemplate.sh`
2. Kjør scriptet: 
   ```
   ./setupTemplate.sh
   ```
3. Fyll inn oppdrag og artifaktnavn (no.nav.sokos.xxx)
4. Endre alarmtypen fra `sokos_ktor_template_type` til f.eks `sokos_din_app_type` i [alerts-dev.yaml](.nais/alerts-dev.yaml) og [alerts-prod.yaml](.nais/alerts-prod.yaml)
5. Endre metric namespace fra `sokos_tor_template` til f.eks `sokos_din_app` i [Metrics.kt](src/main/kotlin/no/nav/sokos/oppdrag/metrics/Metrics.kt)

## Workflows

1. [Deploy alarmer](.github/workflows/alerts-dev.yaml) og [prod](.github/workflows/alerts-prod.yaml) -> For å pushe alarmer for dev og prod
   1 .Gjøre andre nødvendige endringer i alarmer som f.eks navn på applikasjon osv.
   2Denne workflow kjører inviduelt og trigges også hvis det gjøres endringer i [naiserator-dev.yaml](.nais/naiserator-dev.yaml) og [naiserator-prod.yaml](.nais/naiserator-prod.yaml)
2. [Bygg, test og deploy til dev/prod](.github/workflows/deploy.yaml) -> For å bygge/teste prosjektet, bygge/pushe Docker image og deploy til dev og prod
   1. Denne workflow er den aller første som kjøres når kode er i `master/main` branch
3. [Bygg og test PR](.github/workflows/build-pr.yaml) -> For å bygge og teste alle PR som blir opprettet
   1. Denne workflow kjøres kun når det opprettes pull requester
4. [Sikkerhet](.github/workflows/security.yaml) -> For å skanne kode og docker image for sårbarheter. Kjøres hver morgen kl 06:00
   1. Denne kjøres når [Bygg, test og deploy til dev/prodg](.github/workflows/deploy.yaml) har kjørt ferdig

## OpenApi Generator og Swagger
1. Endre [pets.json](https://github.com/navikt/sokos-oppdrag/blob/master/build.gradle.kts#L73) til hva spec filen skal hete som ligger i [specs](specs) mappa.
2. Når prosjektet bygges genereres det data klasser i `build` mappa. Disse sjekkes ikke inn i Git pga. datamodellen kan endres ganske mye så slipper du pushe inn hver endring i modellen. Dvs du følger kontrakten, altså api spec
3. Når du kjører applikasjonen genereres det en SwaggerUI som kan nås på [localhost:8080/api/v1/docs](localhost:8080/api/v1/docs)

## Bygge og kjøre prosjekt
1. Bygg `sokos-oppdrag` ved å kjøre `./gradlew buildFatJar`
2. Start appen lokalt ved å kjøre main metoden i [Bootstrap.kt](src/main/kotlin/no/nav/sokos/oppdrag/Bootstrap.kt)
3. Appen nås på `URL`
4. For å kjøre tester i IntelliJ IDEA trenger du [Kotest IntelliJ Plugin](https://plugins.jetbrains.com/plugin/14080-kotest)

# NB!! Kommer du på noe lurt vi bør ha med i template som default så opprett gjerne en PR 
  
## Henvendelser

- Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på github.
- Interne henvendelser kan sendes via Slack i kanalen [#po-utbetaling](https://nav-it.slack.com/archives/CKZADNFBP)

```
Alt under her skal beholdes som en standard dokumentasjon som må fylles ut av utviklere.
```

# Prosjektnavn

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
Hva er oppgaven til denne applikasjonen

# 2. Utviklingsmiljø
### Forutsetninger
* Java 21
* Gradle 8

### Bygge prosjekt
Hvordan bygger jeg prosjektet.

### Lokal utvikling
Hvordan kan jeg kjøre lokalt og hva trenger jeg?

# 3. Programvarearkitektur
Legg ved skissediagram for hvordan arkitekturen er bygget

# 4. Deployment
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[sokos-oppdrag CI / CD](https://github.com/navikt/sokos-oppdrag/actions)

Push/merge til main branche vil teste, bygge og deploye til produksjonsmiljø og testmiljø.

# 7. Autentisering
Applikasjonen bruker [AzureAD](https://docs.nais.io/security/auth/azure-ad/) autentisering

# 6. Drift og støtte

### Logging
Hvor finner jeg logger? Hvordan filtrerer jeg mellom dev og prod logger?

[sikker-utvikling/logging](https://sikkerhet.nav.no/docs/sikker-utvikling/logging) - Anbefales å lese

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

### Grafana
- [appavn](url)
---

# 7. Swagger
Hva er url til Lokal, dev og prod?

# 8. Henvendelser og tilgang
   Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på Github.
   Interne henvendelser kan sendes via Slack i kanalen #po-utbetaling

