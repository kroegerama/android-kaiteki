import BuildConfig.configurePublish

plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
    signing
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        moduleName = "android.kaiteki.core"
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

    implementation(androidx.bundles.lifecycle)
    implementation(androidx.bundles.navigation)

    implementation(lib.material)

    implementation(androidx.appcompat)
    implementation(androidx.browser)
    implementation(androidx.core)
    implementation(androidx.preference)
    implementation(androidx.recyclerview)
    implementation(androidx.exifinterface)

    implementation(lib.coil)
}

afterEvaluate(configurePublish())