import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport

import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.expediagroup.graphql") version "8.8.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("org.openapi.generator") version "7.13.0"
}

group = "no.nav.sokos"

repositories {
    mavenCentral()

    val githubToken = System.getenv("GITHUB_TOKEN")
    if (githubToken.isNullOrEmpty()) {
        maven {
            name = "external-mirror-github-navikt"
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    } else {
        maven {
            name = "github-package-registry-navikt"
            url = uri("https://maven.pkg.github.com/navikt/maven-release")
            credentials {
                username = "token"
                password = githubToken
            }
        }
    }

    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

// Ktor
val ktorVersion = "3.2.0"

// Serialization
val kotlinxSerializationVersion = "1.8.1"
val kotlinxDatetimeVersion = "0.6.2"
val kotlinxCoroutinesVersion = "1.10.2"

// Monitorering
val micrometerVersion = "1.15.1"

// Logging
val kotlinLoggingVersion = "3.0.5"
val janionVersion = "3.1.12"
val logbackVersion = "1.5.18"
val logstashVersion = "8.1"
val papertrailappVersion = "1.0.0"

// Config
val natpryceVersion = "1.6.10.0"

// Database
val hikariVersion = "6.3.0"
val db2JccVersion = "12.1.2.0"
val kotliqueryVersion = "1.9.1"

// GraphQL
val graphqlClientVersion = "8.8.1"

// Cache
val caffeineVersion = "3.2.1"

// Valkey
val valkeyVersion = "6.7.1.RELEASE"

// TSS
val tjenestespesifikasjonVersion = "1.0_20250414143240_7082707"
val glassfishJaxbVersion = "4.0.5"

// IBM MQ
val ibmMqVersion = "9.4.3.0"

// Test
val kotestVersion = "6.0.0.M4"
val wiremockVersion = "3.13.1"
val mockOAuth2ServerVersion = "2.2.1"
val mockkVersion = "1.14.4"
val swaggerRequestValidatorVersion = "2.44.9"
val testcontainersVersion = "1.21.2"
val h2Version = "2.3.232"
val activemqVersion = "2.41.0"

dependencies {

    // Ktor server
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")

    // Ktor client
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    // Security
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")

    // Monitorering
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.codehaus.janino:janino:$janionVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    runtimeOnly("com.papertrailapp:logback-syslog4j:$papertrailappVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")

    // Database
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.ibm.db2:jcc:$db2JccVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    // GraphQL
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphqlClientVersion") {
        exclude("com.expediagroup:graphql-kotlin-client-jackson")
    }

    // Cache
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    // Redis
    implementation("io.lettuce:lettuce-core:$valkeyVersion")

    // TSS
    implementation("no.nav.sokos.tjenestespesifikasjoner:nav-fim-tss-organisasjon-v4-tjenestespesifikasjon:$tjenestespesifikasjonVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$glassfishJaxbVersion")

    // IBM MQ
    implementation("com.ibm.mq:com.ibm.mq.jakarta.client:$ibmMqVersion")

    // Test
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testImplementation("org.wiremock:wiremock:$wiremockVersion")
    testImplementation("com.atlassian.oai:swagger-request-validator-restassured:$swaggerRequestValidatorVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.apache.activemq:artemis-jakarta-server:$activemqVersion")
}

// Vulnerability fix because of id("org.jlleitschuh.gradle.ktlint") uses ch.qos.logback:logback-classic:1.3.5
configurations.ktlint {
    resolutionStrategy.force("ch.qos.logback:logback-classic:$logbackVersion")
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated/src/main/kotlin")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    named("runKtlintCheckOverMainSourceSet").configure {
        dependsOn("graphqlGenerateClient")
        dependsOn("openApiGenerate")
    }

    named("runKtlintFormatOverMainSourceSet").configure {
        dependsOn("graphqlGenerateClient")
        dependsOn("openApiGenerate")
    }

    withType<KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
        dependsOn("graphqlGenerateClient")
        dependsOn("openApiGenerate")
    }

    withType<ShadowJar>().configureEach {
        enabled = true
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.sokos.oppdrag.ApplicationKt"
            attributes["Class-Path"] = "/var/run/secrets/db2license/db2jcc_license_cisuz.jar"
        }
        finalizedBy(koverHtmlReport)
    }

    withType<KoverReport>().configureEach {
        dependsOn(test)
        kover {
            reports {
                filters {
                    excludes {
                        // exclusion rules - classes to exclude from report
                        classes("no.nav.pdl.*")
                    }
                }
            }
        }
    }

    withType<GenerateTask>().configureEach {
        generatorName.set("kotlin")
        inputSpec.set("$rootDir/src/main/resources/zos/zOsConnectAttestasjon.json")
        outputDir.set("${layout.buildDirectory.get()}/generated")
        modelPackage.set("no.nav.sokos.oppdrag.attestasjon.service.zos")
        configOptions.set(
            mapOf(
                "library" to "jvm-ktor",
                "serializationLibrary" to "kotlinx_serialization",
            ),
        )
        globalProperties.set(
            mapOf(
                "models" to "",
            ),
        )
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }

        reports.forEach { report -> report.required.value(false) }
    }

    withType<GraphQLGenerateClientTask>().configureEach {
        packageName.set("no.nav.pdl")
        schemaFile.set(file("$projectDir/src/main/resources/graphql/schema.graphql"))
        queryFileDirectory.set(file("$projectDir/src/main/resources/graphql"))
        serializer = GraphQLSerializer.KOTLINX
    }

    withType<Wrapper> {
        gradleVersion = "8.14"
    }

    ("jar") {
        enabled = false
    }

    ("build") {
        dependsOn("copyPreCommitHook")
    }

    register<Copy>("copyPreCommitHook") {
        from(".scripts/pre-commit")
        into(".git/hooks")
        filePermissions {
            user {
                execute = true
            }
        }
        doFirst {
            println("Installing git hooks...")
        }
        doLast {
            println("Git hooks installed successfully.")
        }
        description = "Copy pre-commit hook to .git/hooks"
        group = "git hooks"
        outputs.upToDateWhen { false }
    }
}
