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
        moduleName = "android.kaiteki.core"
    }
    buildFeatures {
        buildConfig = false
        viewBinding = true
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

    implementation(androidx.bundles.lifecycle)
    implementation(androidx.bundles.navigation)

    implementation(lib.material)

    implementation(androidx.appcompat)
    implementation(androidx.browser)
    implementation(androidx.core)
    implementation(androidx.preference)
    implementation(androidx.recyclerview)
    implementation(androidx.exifinterface)
}

afterEvaluate(configurePublish())