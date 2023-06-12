plugins {
    com.android.application
    `kotlin-android`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.example"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        applicationId = "com.kroegerama.kaiteki.example"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lintOptions {
//        enable("HiltViewModelAnnotation")
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(lib.kotlin)

    implementation(lib.material)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.constraintlayout)
    implementation(androidx.fragment)
    implementation(androidx.bundles.lifecycle)

    implementation(project(":core"))
    implementation(project(":recyclerview"))
    implementation(project(":retrofit"))
    implementation(project(":views"))

//    debugImplementation(Libs.leakCanary)
    lintChecks(project(":lint"))
}
