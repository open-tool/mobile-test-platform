import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/
plugins {
    application
    distribution
    kotlin("jvm")
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("kapt")
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farm"


java.sourceCompatibility = JavaVersion.VERSION_11


application {
    mainClass.set("com.atiurin.atp.farmserver.FarmServerKt")
    applicationName = "farm-server"
    setBuildDir("build/app")
}
distributions {
    getByName("main") {
        distributionBaseName.set("farm-server")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":farm-core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.8")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.8")
    implementation("org.testcontainers:testcontainers:1.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:2.4.5")
    implementation("org.springframework.boot:spring-boot-starter-web:2.4.5")
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.4.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("io.micrometer:micrometer-core:1.11.5")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.5")
    runtimeOnly("com.h2database:h2:1.4.200")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.4") {
        exclude(module = "mockito-core")
    }
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
kotlin {
    java {
        targetCompatibility = JavaVersion.VERSION_17
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

