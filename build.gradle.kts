plugins {
    kotlin("jvm") version "1.4.32"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("script-runtime"))
}