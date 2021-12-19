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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        moduleName = "android.kaiteki.retrofit"
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
}

dependencies {
    implementation(lib.kotlin)
    implementation(lib.coroutines.android)

    implementation(lib.material)

    implementation(lib.retrofit)

    implementation(androidx.lifecycle.livedata)
    implementation(androidx.paging.runtime)

    implementation(lib.bundles.moshi)
    implementation(lib.timber)

    implementation(project(":core"))
}

afterEvaluate(configurePublish())