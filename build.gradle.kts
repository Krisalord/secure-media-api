val kotlin_version: String by project
val logback_version: String by project
val mongo_version: String by project

val ktorVersion = "3.3.2"
val kmongoVersion = "5.2.0"
val bcryptVersion = "0.4"


plugins {
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

group = "io.github.krisalord"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    // KTOR SERVER
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:${ktorVersion}")

    // DATABASE
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongoVersion")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.13.0") // required by kmongo-coroutine


    // SECURITY
    implementation("org.mindrot:jbcrypt:$bcryptVersion")

    // LOGGING
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // TESTING
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

}
