plugins {
    application
    distribution
    kotlin("jvm")
}

val appVersion: String by project
version = appVersion
group = "com.atiurin.atp.farmcliclient"

application {
    mainClass.set("com.atiurin.atp.farmcliclient.CliAppKt")
    applicationName = "farm-cli-client"
    setBuildDir("build/app")
}

distributions {
    getByName("main") {
        distributionBaseName.set("farm-cli-client")
    }
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":farm-client"))
    implementation(project(":farm-core"))
    implementation("org.apache.commons:commons-exec:1.3")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
//    implementation("ch.qos.logback:logback-classic:1.2.6")
//    implementation("ch.qos.logback:logback-core:1.2.6")
//    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
