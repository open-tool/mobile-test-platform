plugins {
    kotlin("jvm") version "1.5.30"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("script-runtime"))
}