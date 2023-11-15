plugins {
    kotlin("jvm") version "1.9.20"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("script-runtime"))
}
kotlin {
    java {
        targetCompatibility = JavaVersion.VERSION_17
    }
}