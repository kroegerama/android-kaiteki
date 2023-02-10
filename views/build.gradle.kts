import BuildConfig.configurePublish

plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
    signing
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.views"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        moduleName = "android.kaiteki.views"
    }
    buildFeatures {
        buildConfig = false
        viewBinding = true
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
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(lib.kotlin)
    implementation(lib.coroutines.android)

    implementation(androidx.appcompat)
    implementation(androidx.core)

    implementation(androidx.constraintlayout)

    implementation(lib.material)

    implementation(project(":core"))
}

afterEvaluate(configurePublish())