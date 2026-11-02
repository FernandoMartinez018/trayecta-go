plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    alias(libs.plugins.jib)
}

group = "co.trayectago"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "co.trayectago.backend.AplicacionKt"
}

dependencies {
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.callId)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.logback)
    runtimeOnly(libs.logstash)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.ktor.client.contentNegotiation)
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = "eclipse-temurin:21.0.13_9-jre-noble"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/fernandomartinez018/trayecta-go-backend"
    }
    container {
        mainClass = "co.trayectago.backend.AplicacionKt"
        ports = listOf("8080")
        user = "1000:1000"
        jvmFlags = listOf("-XX:MaxRAMPercentage=75")
        environment = mapOf("VERSION" to (findProperty("versionApp") as String? ?: "dev"))
        labels.set(mapOf("org.opencontainers.image.source" to "https://github.com/FernandoMartinez018/trayecta-go"))
    }
}
