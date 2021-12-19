repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation(plg.kotlin)
    implementation(plg.android)

    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}
