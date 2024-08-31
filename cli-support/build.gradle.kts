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
            api(libs.commons.exec)
            implementation(project(":farm-core"))
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}