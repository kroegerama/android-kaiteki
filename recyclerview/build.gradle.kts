plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.recyclerview"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(lib.kotlin)

    implementation(androidx.bundles.lifecycle)
    implementation(androidx.paging.runtime)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.recyclerview)
//    implementation(Libs.androidxViewBinding)
}
