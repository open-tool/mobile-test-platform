import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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