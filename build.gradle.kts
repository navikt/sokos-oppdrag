import kotlinx.kover.gradle.plugin.dsl.tasks.KoverReport

import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    id("com.expediagroup.graphql") version "10.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
    id("org.openapi.generator") version "7.23.0"

    application
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
}

// Ktor
val ktorVersion = "3.5.1"

// Serialization
val kotlinxSerializationVersion = "1.11.0"
val kotlinxDatetimeVersion = "0.8.0-0.6.x-compat"
val kotlinxCoroutinesVersion = "1.11.0"

// Monitorering
val micrometerVersion = "1.17.0"

// Logging
val kotlinLoggingVersion = "3.0.5"
val logbackVersion = "1.5.38"
val logstashVersion = "9.0"

// Config
val natpryceVersion = "1.6.10.0"

// Database
val hikariVersion = "7.1.0"
val db2JccVersion = "12.1.5.0"
val kotliqueryVersion = "2.0.9"

// GraphQL
val graphqlClientVersion = "10.1.2"

// Cache
val caffeineVersion = "3.2.4"

// Valkey
val valkeyVersion = "7.6.0.RELEASE"

// TSS
val tjenestespesifikasjonVersion = "1.0_20260311085905_5544925"
val glassfishJaxbVersion = "4.0.9"

// IBM MQ
val ibmMqVersion = "10.0.0.0"

// Test
val kotestVersion = "6.2.2"
val wiremockVersion = "3.13.2"
val mockOAuth2ServerVersion = "5.0.2"
val mockkVersion = "1.14.11"
val swaggerRequestValidatorVersion = "3.0.0"
val testcontainersVersion = "2.0.5"
val h2Version = "2.4.240"
val activemqVersion = "2.55.0"

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
    implementation("io.ktor:ktor-client-apache5-jvm:$ktorVersion")

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
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    // Config
    implementation("com.natpryce:konfig:$natpryceVersion")

    // Database
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.ibm.db2:jcc:$db2JccVersion")
    implementation("no.nav:kotliquery:$kotliqueryVersion")

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

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "io.netty") {
                useVersion("4.2.15.Final")
                because("Multiple versions of netty has vulnerable dependencies. Affected version < 4.2.15.Final")
            }
            if (requested.group == "tools.jackson.core" && requested.name == "jackson-databind") {
                useVersion("3.2.0")
                because("Multiple versions of jackson-databind has vulnerable dependencies. Affected version < >= 3.0.0, <= 3.1.3")
            }
            if (requested.group == "tools.jackson.core" && requested.name == "jackson-core") {
                useVersion("3.2.0")
                because("Multiple versions of jackson-core has vulnerable dependencies. Affected version >= 3.0.0, <= 3.1.0")
            }
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-databind") {
                useVersion("2.21.4")
                because("Multiple versions of jackson-databind has vulnerable dependencies. Affected version >= 2.19.0, <= 2.21.3")
            }
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-core") {
                useVersion("2.21.4")
                because("Multiple versions of jackson-core has vulnerable dependencies.. Affected version >= 2.19.0, <= 2.21.1")
            }
        }
    }
}

application {
    mainClass.set("no.nav.sokos.oppdrag.ApplicationKt")
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
        languageVersion.set(JavaLanguageVersion.of(25))
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

        finalizedBy(koverHtmlReport)
    }

    withType<GraphQLGenerateClientTask>().configureEach {
        packageName.set("no.nav.pdl")
        schemaFile.set(file("$projectDir/src/main/resources/graphql/schema.graphql"))
        queryFileDirectory.set(file("$projectDir/src/main/resources/graphql"))
        serializer = GraphQLSerializer.KOTLINX
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
