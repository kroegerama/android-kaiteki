import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kaiteki.publishing-conventions")
}

android {
    compileSdk = Android.COMPILE_SDK
    namespace = "com.kroegerama.kaiteki.retrofit"

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
        moduleName = "android.kaiteki.retrofit"
        jvmTarget = JvmTarget.JVM_11
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
    coreLibrariesVersion = "2.0.21"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.material)

    implementation(libs.retrofit)

    implementation(libs.fragment)
    implementation(libs.lifecycle.livedata)
    implementation(libs.paging.runtime)

    implementation(libs.compose.runtime)

    implementation(libs.bundles.moshi)

    implementation(libs.arrow.core)

    implementation(project(":core"))

    coreLibraryDesugaring(libs.desugar)
}
