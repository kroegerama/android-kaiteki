import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    alias(magic.plugins.kotlin.android)
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.example"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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

//    lint {
//        enable += listOf(
//            "AndroidEntryPointAnnotation", "HiltViewModelAnnotation"
//        )
//    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    val jvmVersion: String by project
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(jvmVersion)
    }
}

dependencies {
    implementation(magic.kotlin.stdlib.jdk8)

    implementation(magic.material)

    implementation(androidx.appcompat)
    implementation(androidx.core)
    implementation(androidx.constraintlayout)
    implementation(androidx.fragment)
    implementation(androidx.bundles.lifecycle)

    implementation(project(":core"))
    implementation(project(":recyclerview"))
    implementation(project(":retrofit"))
    implementation(project(":views"))

    coreLibraryDesugaring(magic.desugar)

//    debugImplementation(Libs.leakCanary)
//    lintChecks(project(":lint"))
}
