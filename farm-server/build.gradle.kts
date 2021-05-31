import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm")
    kotlin("plugin.spring") version "1.4.32"
}



group = "com.atiurin.atp.farm"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":farm-core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.8")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.8")
    implementation("org.testcontainers:testcontainers:1.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
	runtimeOnly("com.h2database:h2")
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
//        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}
