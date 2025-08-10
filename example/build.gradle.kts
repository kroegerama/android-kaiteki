import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = Android.COMPILE_SDK
    namespace = "com.kroegerama.kaiteki.example"

    defaultConfig {
        applicationId = "com.kroegerama.kaiteki.example"
        minSdk = Android.MIN_SDK
        targetSdk = Android.TARGET_SDK
        versionCode = 1
        versionName = "1.0"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.material)

    implementation(libs.appcompat)
    implementation(libs.core)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.bundles.lifecycle)

    implementation(project(":core"))
    implementation(project(":recyclerview"))
    implementation(project(":retrofit"))
    implementation(project(":views"))

    coreLibraryDesugaring(libs.desugar)

//    debugImplementation(Libs.leakCanary)
//    lintChecks(project(":lint"))
}
