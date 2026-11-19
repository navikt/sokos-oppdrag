---
name: gradle-sarbarheter
description: Fiks åpne Dependabot-varsler og kontroller om eksisterende sikkerhetsconstraints fortsatt trengs i Gradle/Kotlin-prosjekter. Bruk ved «Fiks sårbarheter», «fiks Dependabot-varsler», «sjekk constraints» og «fjern overflødige constraints». Gjør bare nødvendige endringer knyttet til varsler eller eksisterende constraints. Ikke utfør en generell sårbarhetsskanning. Kjør relevante tester og bygg ved endringer, uten rapportgodkjenning først. La endringene være ustaget, uten commit eller push.
compatibility: Krever Gradle Wrapper, riktig JDK og gh med tilgang til repoet. jq brukes til å lese JSON-rapporter.
metadata:
  domain: platform
  tags: gradle kotlin vulnerabilities dependabot constraints bom
---

# Fiks sårbarheter i Gradle/Kotlin

Utfør kartlegging, retting, opprydding og validering i samme oppdrag. Brukeren gjennomgår diffen og gjør selv `git add`, commit og push. Ikke vent på rapportgodkjenning før vanlige avhengighetsendringer. Hvis brukeren uttrykkelig ber om bare en rapport, skal du ikke endre filer.

Støtt bare Gradle/Kotlin. Ikke oppgrader andre økosystemer. Ikke endre vaktmesterdokumentasjon eller andre repoer.

## Avgrensning

Oppdraget har bare to utgangspunkt:

- Åpne Dependabot-varsler i målrepoet og innenfor brukerens mappeavgrensning.
- Eksisterende sikkerhetsconstraints i repoet: kontroller om de fortsatt trengs, og behold, juster eller fjern dem ut fra faktisk avhengighetsgraf og gjeldende advisories.

Hver endring skal kunne knyttes til et konkret Dependabot-varsel eller en eksisterende sikkerhetsconstraint. Nye constraints er bare tillatt når de er nødvendige for å rette et åpent Dependabot-varsel. BOM-er og direkte avhengigheter endres bare når de styrer versjoner som inngår i denne avgrensningen.

Ikke skann alle avhengigheter, plugin-er eller byggeverktøy for å finne flere oppgaver. Nye funn utenfor avgrensningen som dukker opp underveis, rapporteres separat og fører ikke til endringer uten at brukeren utvider oppdraget. Ikke legg for eksempel til en ny ktlint-/Logback- eller Kover-/FreeMarker-constraint bare fordi et advisory-oppslag viser et funn som ikke finnes blant de åpne Dependabot-varslene.

En tom Dependabot-liste er ikke bevis for at alle avhengigheter er uten sårbarheter. Kontroller fortsatt eksisterende sikkerhetsconstraints, men ikke utvid søket. Hvis ingen varsler krever retting og ingen eksisterende constraints bør endres, avslutt uten filendringer.

## 1. Avgrens prosjektet og beskytt lokalt arbeid

- Følg repoets instruksjoner. Finn Gradle Wrapper, byggfiler, settings, versjonskataloger, eventuell dependency locking og dependency verification.
- Finn Gradle-roten og berørte subprosjekter. Et GitHub-repo kan inneholde flere prosjekter og økosystemer. Respekter brukerens mappeavgrensning.
- Registrer Git-root, branch, commit og eksisterende staged/unstaged/untracked endringer. Utled GitHub-repo fra remote eller `gh repo view`, aldri fra et hardkodet reponavn. Spør hvis målrepoet er tvetydig.
- Bevar brukerens endringer og indeks. Ikke stash, stage, commit, push, opprett PR, deploy eller lukk/avvis varsler. Ikke bruk reset eller checkout for å rydde.
- Bruk repoets `./gradlew` og JDK. Lag hjelpefiler og JSON-rapporter i agentens sesjonsmappe, ikke i prosjektet. Finn `scripts/dependency-audit.init.gradle` relativt til denne SKILL.md, ikke via en brukerspesifikk sti.
- Les hvilke byggoppgaver som formaterer kode, installerer Git hooks eller har andre sideeffekter før du kjører dem. Ikke tillat at en avhengighetsoppgave overskriver brukerens arbeid.

## 2. Hent sårbarhetsdata

Bruk `gh` med eksisterende innlogging. Ikke skriv ut token eller andre secrets. Kommandoene nedenfor har plassholdere: erstatt dem med verifiserte verdier og bruk lokale kommandoregler, inkludert `rtk` der det kreves.

Hent alle åpne Dependabot-varsler, inkludert alle sider:

```sh
gh api --method GET --paginate --slurp \
  'repos/OWNER/REPO/dependabot/alerts?state=open&per_page=100' \
  > SESSION/dependabot-open.json
```

`--slurp` gir en liste med sider; bruk `jq '[.[][]]'` for å flate den ut. Registrer tidspunkt, repo, varselnummer/URL, manifeststi, økosystem, pakke, valgt versjon hvis oppgitt, CVE/GHSA, alvorlighet, sårbart intervall og første rettede versjon. Skill varsler utenfor mappeavgrensningen fra dem som behandles.

HTTP-feil, manglende rettigheter, avslått tilgang eller ufullstendig paginering betyr **ikke kontrollert**, aldri «ingen varsler». Ikke omgå tilgangsavslag. Fortsett bare med kontroller som har tilstrekkelig datagrunnlag, og rapporter blokkeringen.

Dependabot er knyttet til GitHubs registrerte dependency graph, ikke nødvendigvis den lokale branchen. Bruk advisory-oppslag til å verifisere pakkene som inngår i åpne varsler eller eksisterende sikkerhetsconstraints. Kontroller også alle nye eller endrede modulversjoner som en foreslått retting eller constraint-fjerning medfører. Dette er validering av den aktuelle endringen, ikke en generell sårbarhetsskanning:

```sh
gh api --method GET --paginate --slurp /advisories \
  -f ecosystem=maven -f 'affects=GROUP:ARTIFACT@VERSION' -f per_page=100 \
  > SESSION/advisories-package-version.json
```

Bruk faktisk Gradle-gruppe, artefakt og valgt versjon. API-et skal evaluere versjonen; ikke bruk tekstsortering, generisk SemVer eller en hjemmelaget Maven-versjonssammenligner. Hent advisory-detaljer ved behov. Skill trukne advisories fra aktive, og behandle manglende eller tvetydige versjonsgrenser som uavklart.

En vellykket tom respons betyr «ingen treff i den sjekkede kilden», ikke «pakken er sikker». Oppgi kildedekningen. Ikke utvid oppdraget med en separat Nais/TPT- eller image-/SBOM-kontroll uten at brukeren ber om det. Ikke påstå at lokal branch beskriver deployet image uten image-/SBOM-bevis. Ikke legg til en skanner eller en ny avhengighet.

## 3. Beregn faktisk avhengighetstre

Les deklarasjoner av direkte avhengigheter, constraints og BOM-er. En vanlig constraint angir et versjonskrav, ikke nødvendigvis én eksakt valgt versjon. Bruk Gradles beregning som bevis. Et prosjekt uten låsefil kan undersøkes; rapporter da at valgene ikke er låst.

Hjelpescriptet har to oppgaver. Kjør dem separat, med hver sin absolutte JSON-sti i sesjonsmappen:

```sh
./gradlew --no-configuration-cache --console=plain \
  -I SKILL/scripts/dependency-audit.init.gradle \
  -PvulnerabilityAuditOutput=SESSION/inventory.json \
  vulnerabilityAuditInventory

./gradlew --no-configuration-cache --console=plain \
  -I SKILL/scripts/dependency-audit.init.gradle \
  -PvulnerabilityAuditOutput=SESSION/baseline.json \
  -PvulnerabilityAuditConfigurations=:runtimeClasspath,:testRuntimeClasspath \
  vulnerabilityAuditGraph
```

Velg konfigurasjonene fra inventaret, ikke fra eksempelkommandoen alene. Et subprosjekt bruker for eksempel `:subprosjekt:runtimeClasspath`. Dekk berørte compile-/runtime- og testkonfigurasjoner. Ta med kodegeneratorer, plugin-er og byggeverktøy bare når et åpent varsel eller en eksisterende sikkerhetsconstraint gjelder disse konfigurasjonene. Ikke løs alle konfigurasjoner ukritisk: de kan kreve andre plattformer eller miljøer. Oppgi hvilke berørte konfigurasjoner som er kontrollert og hvilke som mangler.

Inventaret viser også buildscript-konfigurasjoner. Graph-oppgaven undersøker prosjektkonfigurasjoner, ikke alle plugin-/buildscript-avhengigheter, inkluderte builds eller separate Gradle-røtter. Hvis et åpent varsel eller en eksisterende sikkerhetsconstraint gjelder disse, undersøk dem separat med Gradles egne rapporter, eksempelvis `buildEnvironment`, eller rapporter manglende dekning. Ellers skal de ikke undersøkes.

Bruk `dependencyInsight --configuration CONFIG --dependency GROUP:ARTIFACT` i riktig subprosjekt når du trenger mer om avhengighetssti, BOM-alignment eller valggrunn. Registrer også alternative avhengighetsstier, ikke bare den første.

`--offline` kan brukes for diagnostikk, men ikke som bevis for oppdaterte sårbarhetsdata eller at en ny versjon finnes. Uavklarte avhengigheter gjør den aktuelle konklusjonen uavklart, også når Gradles vanlige rapportoppgave avslutter med kode 0.

## 4. Rett funn uten unødvendige oppgraderinger

Koble hvert åpent Dependabot-varsels advisory og manifeststi til faktisk valgt pakke og konfigurasjon. Knytt vedlikehold av eksisterende sikkerhetsconstraints til kontrollen i steg 5. Skill runtime, test og byggeverktøy. Åpne test-/build-varsler behandles på samme måte som runtime-varsler, men nye funn utenfor avgrensningen rapporteres bare.

Prioriter slik:

1. Oppgrader den direkte avhengigheten som trekker inn pakken, til en verifisert stabil versjon som løser funnet.
2. Oppgrader en allerede brukt BOM når den er riktig eier av versjonen og endringen kan verifiseres.
3. Legg til en målrettet constraint for et åpent Dependabot-varsel, eller juster en eksisterende sikkerhetsconstraint, hvis de to første ikke løser problemet innenfor oppdraget.

### Automatisk versjonskontroll

Agenten skal hente og verifisere versjoner selv, ikke overlate den ordinære versjonskontrollen til utvikleren:

- Finn aktuelle stabile versjoner fra pakkens faktiske Maven-repository. Bruk repository-metadata og hent POM eller Gradle-metadata for den konkrete versjonen. Velg den minste nødvendige stabile patch-/minor-endringen som løser funnet og består de øvrige kontrollene, ikke automatisk nyeste versjon.
- Bekreft med Gradle hvilken versjon som faktisk velges i hver berørt konfigurasjon. Skill deklarert versjon fra valgt versjon.
- Kontroller versjonen mot gjeldende advisories som beskrevet i steg 2. At en versjon finnes i Maven, betyr ikke at den er uten kjente sårbarheter.
- Registrer tidspunkt og en lenke til den konkrete versjonens POM, Gradle-metadata eller versjonsside i repositoryet. Bruk lenken i sluttmatrisen, slik at utvikleren kan etterprøve kontrollen uten å gjøre den på nytt.

MvnRepository kan brukes som supplerende navigasjonslenke, men er ikke autoritativt bevis for at en versjon finnes i repositoryet prosjektet bruker. Et mislykket repository-oppslag kan skyldes nettverk eller rettigheter; ikke konkluder automatisk med at versjonen ikke finnes. Rapporter «ikke kontrollert» eller «uavklart» hvis kontrollen ikke kan fullføres. Ikke gjett versjoner, konstruer uverifiserte kildelenker eller bytt repository for å få et grønt resultat. Lenker skal ikke inneholde tokens eller annen innloggingsinformasjon.

Følg eksisterende struktur og eierskap i byggfil/versjonskatalog. Ikke tving hele bibliotekfamilier til samme versjon uten støtte i leverandørens BOM. Plasser constraints i konfigurasjonene som faktisk trenger dem, inkludert separate verktøykonfigurasjoner ved behov. En `implementation`-constraint dekker ikke automatisk alle konfigurasjoner.

Nye eller justerte sikkerhetsconstraints skal ha `because(...)` med CVE/GHSA, sårbart intervall, kilde og hva som må være oppfylt før de kan fjernes. Ikke omskriv begrunnelser på ellers uendrede constraints bare for å standardisere teksten. Ikke innfør andre overstyringsmekanismer eller skjul varsler med ekskluderinger.

Vanlige patch-/minor-endringer utføres uten ny godkjenning. Ikke endre auth-/forretningskode, gjennomfør framework-/major-migrasjoner, oppgrader Gradle/JDK eller endre CI, Docker, Nais eller serverversjoner som del av dette oppdraget. Hvis en slik endring er nødvendig: rapporter det konkrete funnet som blokkert og fortsett med uavhengige rettinger.

Oppdater eksisterende låser målrettet for berørte pakker og alle berørte låste konfigurasjoner. Ikke slå av locking, strict constraints eller dependency verification for å få et ønsket resultat. Kontroller at låser ikke holder igjen sårbare versjoner. Oppdater eventuell verification-metadata etter repoets rutine, og verifiser nye artefakter mot en betrodd kilde, ikke bare lokale kontrollsummer.

## 5. Vurder fjerning av eksisterende constraints

Undersøk alle sikkerhetsconstraints innenfor oppdraget, også når ingen åpne Dependabot-varsler finnes. En gammel `because(...)` forklarer hensikten, men er ikke oppdatert sikkerhetsbevis.

Lagre et nytt referansetre etter eventuelle rettinger. Prøv så én pakke i ett eierprosjekt om gangen, med samme konfigurasjonsdekning som referansen:

```sh
./gradlew --no-configuration-cache --console=plain \
  -I SKILL/scripts/dependency-audit.init.gradle \
  -PvulnerabilityAuditOutput=SESSION/without-one-constraint.json \
  -PvulnerabilityAuditConfigurations=:runtimeClasspath,:testRuntimeClasspath \
  -PvulnerabilityAuditDropConstraint=GROUP:ARTIFACT \
  -PvulnerabilityAuditConstraintProject=: \
  vulnerabilityAuditGraph
```

Dette fjerner bare pakkens lokalt deklarerte constraints i valgt prosjekt under beregningen. Flere konfigurasjoner kan inneholde samme pakke; les listen over fjernede deklarasjoner. Avhengigheter, BOM-er og filer endres ikke. En constraint fra en ekstern plattform fjernes ikke av scriptet.

Sammenlign hele grafen: alle nye eller endrede modulversjoner, forsvunne komponenter og uløste avhengigheter. Ikke vurder bare pakken constrainten navngir. Kontroller gjenværende versjon mot den opprinnelige advisoryen og spør advisory-kilden om alle nye eller endrede modulversjoner. Hvis fjerningen gir andre sårbarhetstreff, behold constrainten og forklar hvorfor. Ikke opprett nye rettinger for å få fjernet den.

Fjern constrainten fra byggfilen bare når:

- Hele den berørte grafen kan beregnes, og den opprinnelige sårbarheten fortsatt er håndtert.
- Kontrollerte kilder ikke viser andre uløste sårbarheter i kandidatversjonene. Manglende datagrunnlag gir «uavklart».
- Constrainten ikke har en annen nødvendig hensikt, som kompatibilitet eller plattform-alignment.
- Resultatet ikke bare skyldes en låsefil som fortsatt holder den gamle overstyrte versjonen. Ved locking må naturlig versjonsvalg prøves uten den aktuelle låsepinnen i en tillatt, isolert og midlertidig beregning. Hvis dette ikke kan gjøres trygt, behold constrainten som uavklart.

At samme versjon velges uten constraint, er heller ikke nok alene: skill mellom en redundant sikkerhetsconstraint og et tilsiktet framtidig minimumskrav. Ikke fjern eksplisitte policykrav uten at oppdraget dekker det.

Etter hver godtatt fjerning oppdateres referansen før neste forsøk. Valider også alle endringene samlet. To hver for seg vellykkede fjerninger kan påvirke hverandre.

## 6. Verifiser og lever

- Beregn endelig graf for alle berørte konfigurasjoner. Sammenlign den med den opprinnelige grafen, og kontroller alle nye/endrede modulversjoner mot advisory-kilden. Ikke stol på at Dependabot oppdateres før endringene er pushet.
- Ved filendringer: kjør repoets eksisterende relevante lint-, test-, bygg- og pakkeoppgaver. Bruk `./gradlew build` når den er repoets verifikasjon; kjør også nødvendige separate oppgaver hvis de ikke inngår. Ikke hopp over eller svekk tester. Hvis kontrollen ikke fører til filendringer, er avhengighetsberegningene og advisory-oppslagene tilstrekkelige; ikke kjør et fullt bygg bare for å bekrefte at ingenting er endret.
- Skill baseline-feil fra feil endringen introduserer. Hvis tester ikke kan kjøres, er endringen ikke ferdig verifisert. Ikke rapporter grønt fordi avhengighetstreet alene kan beregnes.
- Ved feil: rett bare direkte relaterte avhengighetsproblemer innenfor avgrensningen. Hvis en pakke ikke kan fikses, fjern bare dine egne mislykkede endringer med en presis patch. Bevar brukerens arbeid og uavhengige vellykkede rettinger.
- Inspiser endelig diff, staged-diff og nye filer. Fjern utilsiktet formatering og genererte filer du selv har laget. Ikke endre eksisterende staged innhold. Nye endringer skal være ustaget.
- Ikke lag README, rapportfiler eller andre leveransedokumenter i repoet. Fjern egne midlertidige hjelpefiler som ikke trengs for etterprøvbarhet. Ikke slett brukerfiler.

### Obligatorisk resultatmatrise i svaret

Vis alltid resultatet i agentens avsluttende output, også når ingen filer er endret. Ikke lag en rapportfil i repoet. Start med antall åpne Dependabot-varsler, antall endringer og om verifikasjonen er fullført eller blokkert.

Bruk denne matrisen med én rad per behandlet pakke/varsel eller eksisterende sikkerhetsconstraint:

| Pakke / varsel | Før | Etter | Handling og begrunnelse | Versjonskilde | Status |
|---|---|---|---|---|---|
| `gruppe:artefakt`, lenke til Dependabot-varsel og CVE/GHSA hvis relevant | Faktisk valgt versjon før | Faktisk valgt versjon etter | Oppgradert, constraint lagt til/justert/fjernet/beholdt, eller ingen endring, med kort begrunnelse | Verifisert lenke til konkret versjon i pakkens Maven-repository | Verifisert, delvis verifisert, blokkert eller ikke kontrollert |

- Ta med beholdte constraints og forklar hvorfor de fortsatt trengs. Ved fjernet constraint viser «Etter» versjonen Gradle velger uten den, ikke bare at deklarasjonen er borte.
- Hvis konfigurasjonene velger ulike versjoner, oppgi dem med konfigurasjonsnavn eller bruk separate rader. Ikke oppgi én versjon som om den gjelder alle.
- Skill utført endring fra verifikasjonsstatus. En endret byggfil betyr ikke at funnet er ferdig verifisert. Bruk «delvis verifisert» eller «blokkert» når relevante tester eller kildekontroller mangler.
- Hvis det verken finnes åpne varsler eller eksisterende sikkerhetsconstraints, skriv «Ingen tiltak» i en oppsummeringsrad. Ikke finn på pakker eller versjoner for å fylle matrisen.

Oppgi under matrisen hvilke verifikasjonskommandoer som ble kjørt og resultatet, kilde/tidspunkt, konfigurasjonsdekning og eventuelle uløste funn innenfor avgrensningen. Hvis ingen filendringer var nødvendige, si at tester/bygg ikke ble kjørt av den grunn. Eventuelle tilleggsfunn føres separat som «utenfor oppdraget, ikke endret», ikke som Dependabot-varsler. Ikke presenter dem som utførte rettinger eller utvid oppdraget for å fylle matrisen. «Ingen kjente treff i kontrollerte kilder» er ikke en garanti om fravær av sårbarheter.

## Kontroll av skillen

Ved første bruk: kontroller at et kjent varsel fra GitHub blir med når et slikt finnes, at nødvendige constraints beholdes og at eventuelle redundante constraints blir identifisert. Kontroller også at utilgjengelige kilder og uavklarte avhengigheter gir «ikke kontrollert» eller «uavklart», ikke et grønt resultat. Ved tom Dependabot-liste skal bare eksisterende sikkerhetsconstraints vurderes. Ikke legg til nye constraints for funn utenfor oppdraget, og ikke endre filer når ingen retting eller opprydding er nødvendig. Sluttmatrisen skal vise både utførte tiltak og begrunnede beslutninger om å beholde constraints, med faktisk valgte versjoner, verifiserte versjonslenker og ærlig verifikasjonsstatus.
