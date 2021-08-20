import BuildConfig.configurePublish

plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
    signing
}

android {
    compileSdk = Android.compileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        moduleName = "android.kaiteki.recyclerview"
    }
    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdk = Android.minSdk
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
