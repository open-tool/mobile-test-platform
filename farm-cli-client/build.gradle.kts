plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":farm-client"))
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
