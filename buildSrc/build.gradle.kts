repositories {
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    val kotlinVersion: String by project
    val androidGradleVersion: String by project
    val bintrayVersion: String by project

    implementation(gradleApi())
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("com.android.tools.build:gradle:$androidGradleVersion")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:$bintrayVersion")
}