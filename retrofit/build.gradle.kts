plugins {
    com.android.library
    `kotlin-android`
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.retrofit"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        moduleName = "android.kaiteki.retrofit"
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
    implementation(lib.coroutines.android)

    implementation(lib.material)

    implementation(lib.retrofit)

    implementation(androidx.lifecycle.livedata)
    implementation(androidx.paging.runtime)

    implementation(lib.bundles.moshi)
    implementation(lib.timber)

    implementation(project(":core"))
}
