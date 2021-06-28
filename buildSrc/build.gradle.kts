repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.kroegerama:magic-bom:1.1.10"))

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle:7.1.0-alpha02")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}
