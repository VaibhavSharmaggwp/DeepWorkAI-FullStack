val logback_version: String by project
val ktor_version = "3.0.3"
val kotlin_version = "2.0.0"
val exposed_version = "0.41.1"

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    // We removed the io.ktor.plugin here to avoid the 'convention' error
    id("application")
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {

    // Google API Client for verifying ID Tokens
    implementation("com.google.api-client:google-api-client:2.2.0")



    // JSON support for the Google client
    implementation("com.google.http-client:google-http-client-gson:1.44.1")
    // Ktor Server Core & Netty
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    // Exposed Framework
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")

    // Password hashing library
    implementation("org.mindrot:jbcrypt:0.4")

    // Google API Client for token verification
    implementation("com.google.api-client:google-api-client:2.2.0")

    // Database & Utils
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Ktor JWT Authentication
    implementation("io.ktor:ktor-server-auth-jwt-jvm:${ktor_version}")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

kotlin {
    jvmToolchain(17)
}