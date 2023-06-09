import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/
plugins {
    application
    distribution
    kotlin("jvm")
    id("org.springframework.boot") version "2.4.5" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.6.0"
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farm"


java.sourceCompatibility = JavaVersion.VERSION_11


application {
    mainClass.set("com.atiurin.atp.farmserver.FarmApplicationKt")
    applicationName = "farm-server"
    setBuildDir("build/app")
}
distributions {
    getByName("main") {
        distributionBaseName.set("farm-server")
    }
}

repositories {
    mavenCentral()
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    runtimeOnly("com.h2database:h2:1.4.200")
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

