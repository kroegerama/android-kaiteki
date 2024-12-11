import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.retrofit"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdk = Android.minSdk
    }
    testOptions {
        targetSdk = Android.targetSdk
    }
    lint {
        targetSdk = Android.targetSdk
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

kotlin {
    val jvmVersion: String by project
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(jvmVersion)
        moduleName = "android.kaiteki.retrofit"
    }
}

dependencies {
    implementation(magic.kotlin.stdlib.jdk8)
    implementation(magic.kotlinx.coroutines.android)

    implementation(magic.material)

    implementation(magic.retrofit)

    implementation(androidx.fragment)
    implementation(androidx.lifecycle.livedata)
    implementation(androidx.paging.runtime)

    implementation(magic.bundles.moshi)

    implementation(magic.bundles.arrow)

    implementation(project(":core"))

    coreLibraryDesugaring(magic.desugar)
}
