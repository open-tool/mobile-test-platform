plugins {
    alias(libs.plugins.kotlinMultiplatform)
}
repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    mavenCentral()
    google()
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            api(libs.commons.exec)
            implementation(project(":farm-core"))
            implementation(libs.ktorClientCore)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}