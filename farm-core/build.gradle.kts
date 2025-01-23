
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
    java {
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets{
        commonMain {
            dependencies{
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlin.logging)
            }
        }
//        jvmMain {
//            dependencies {
//                implementation(libs.slf4j.simple)
//            }
//        }
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}