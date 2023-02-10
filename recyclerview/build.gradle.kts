import BuildConfig.configurePublish

plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
    signing
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.recyclerview"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        moduleName = "android.kaiteki.recyclerview"
    }
    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdk = Android.minSdk
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
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(lib.kotlin)

    implementation(androidx.paging.runtime)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.recyclerview)
//    implementation(Libs.androidxViewBinding)
}

afterEvaluate(configurePublish())
