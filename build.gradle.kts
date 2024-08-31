plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}
//dependencies {
//    implementation(kotlin("script-runtime"))
//}
//kotlin {
//    java {
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//}