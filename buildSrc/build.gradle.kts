repositories {
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.kroegerama:magic-bom:1.0.4"))

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}