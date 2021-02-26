repositories {
    google()
    mavenCentral()
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
    implementation(platform("com.kroegerama:magic-bom:1.1.1"))
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle")
}