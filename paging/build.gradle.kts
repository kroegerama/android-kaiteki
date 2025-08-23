import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kaiteki.publishing-conventions")
}

android {
    compileSdk = Android.COMPILE_SDK
    namespace = "com.kroegerama.kaiteki.paging"

    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdk = Android.MIN_SDK
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    testOptions {
        targetSdk = Android.TARGET_SDK
    }
    lint {
        targetSdk = Android.TARGET_SDK
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        moduleName = "android.kaiteki.paging"
        jvmTarget = JvmTarget.JVM_11
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
    coreLibrariesVersion = "2.0.21"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.bundles.lifecycle)

    implementation(libs.appcompat)
    implementation(libs.core)
    implementation(libs.paging.runtime)
    implementation(libs.recyclerview)

    implementation(libs.swiperefreshlayout)

    implementation(libs.retrofit)
    implementation(libs.arrow.core)

    implementation(project(":core"))
    implementation(project(":retrofit"))
    implementation(project(":recyclerview"))

    coreLibraryDesugaring(libs.desugar)
}
