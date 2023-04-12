import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id ("org.jetbrains.kotlin.jvm")
}


group = "com.atiurin.atp.runner"
version = "0.0.1"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.malinskiy.adam:adam:0.5.0")
    implementation("com.google.guava:guava:26.0-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation ("com.squareup.retrofit2:retrofit:2.7.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.7.0")
    implementation ("com.squareup.okhttp3:okhttp:4.3.1")
    implementation ("com.google.code.gson:gson:2.8.5")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
