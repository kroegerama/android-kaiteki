repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.trove4j:trove4j:20160824")).using(module("org.jetbrains.intellij.deps:trove4j:1.0.20181211"))
    }
}

dependencies {
    implementation(platform("com.kroegerama:magic-bom:1.1.4"))

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle")
    implementation("io.github.gradle-nexus:publish-plugin:1.0.0")
}
