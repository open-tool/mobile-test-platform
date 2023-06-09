plugins {
    kotlin("jvm")
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farmcore"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
