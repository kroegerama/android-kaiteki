plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
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
    implementation(magic.kotlin.stdlib.jdk8)
    implementation(magic.kotlinx.coroutines.android)

    implementation(magic.material)

    implementation(magic.retrofit)

    implementation(androidx.fragment)
    implementation(androidx.lifecycle.livedata)
    implementation(androidx.paging.runtime)

    implementation(magic.bundles.moshi)
    implementation(magic.timber)

    implementation(magic.bundles.arrow)

    implementation(project(":core"))
}
