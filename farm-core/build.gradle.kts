
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farmcore"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    mavenCentral()
    google()
}

kotlin {
    jvm()
    wasmJs()
    java {
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets{
        commonMain {
            dependencies{
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}