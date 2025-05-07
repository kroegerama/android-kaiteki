import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.recyclerview"

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
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
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
        moduleName = "android.kaiteki.recyclerview"
    }
}

dependencies {
    implementation(magic.kotlin.stdlib.jdk8)

    implementation(androidx.bundles.lifecycle)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.recyclerview)

    implementation(magic.material)

    coreLibraryDesugaring(magic.desugar)
}
