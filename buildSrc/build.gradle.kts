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
    implementation("com.android.tools.build:gradle") {
        version {
            require("7.0.0")
        }
    }
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}
