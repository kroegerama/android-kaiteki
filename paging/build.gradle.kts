plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.paging"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        moduleName = "android.kaiteki.paging"
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
    implementation(magic.kotlin.stdlib.jdk8)
    implementation(magic.kotlinx.coroutines.android)

    implementation(androidx.bundles.lifecycle)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.paging.runtime)
    implementation(androidx.recyclerview)

    implementation(androidx.swiperefreshlayout)

    implementation(magic.retrofit)
    implementation(magic.bundles.arrow)

    implementation(project(":core"))
    implementation(project(":retrofit"))
    implementation(project(":recyclerview"))

    coreLibraryDesugaring(magic.desugar)
}
