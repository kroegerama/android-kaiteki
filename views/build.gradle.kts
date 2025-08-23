import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kaiteki.publishing-conventions")
}

android {
    compileSdk = Android.COMPILE_SDK
    namespace = "com.kroegerama.kaiteki.views"

    buildFeatures {
        buildConfig = false
        viewBinding = true
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
        moduleName = "android.kaiteki.views"
        jvmTarget = JvmTarget.JVM_11
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
    coreLibrariesVersion = "2.0.21"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.appcompat)
    implementation(libs.core)

    implementation(libs.constraintlayout)

    implementation(libs.material)

    implementation(project(":core"))

    coreLibraryDesugaring(libs.desugar)
}
