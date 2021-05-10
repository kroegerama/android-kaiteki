repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.kroegerama:magic-bom:1.1.7"))

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle")
    implementation("io.github.gradle-nexus:publish-plugin:1.0.0")
}
