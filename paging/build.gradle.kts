import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.paging"

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
        moduleName = "android.kaiteki.paging"
    }
}

dependencies {
    implementation(magic.kotlin.stdlib.jdk8)
    implementation(magic.kotlinx.coroutines.android)

    implementation(androidx.bundles.lifecycle)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.paging.runtime)
    implementation(androidx.recyclerview)

    implementation(androidx.swiperefreshlayout)

    implementation(magic.retrofit)
    implementation(magic.bundles.arrow)

    implementation(project(":core"))
    implementation(project(":retrofit"))
    implementation(project(":recyclerview"))

    coreLibraryDesugaring(magic.desugar)
}
