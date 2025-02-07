import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(magic.plugins.kotlin.android)
    alias(magic.plugins.kotlin.compose)
    `maven-publish`
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kroegerama.kaiteki.compose"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = false
        compose = true
    }

    defaultConfig {
        minSdk = Android.minSdkCompose
    }
    testOptions {
        targetSdk = Android.targetSdk
    }
    lint {
        targetSdk = Android.targetSdk
    }

    buildTypes {
        release {
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
    val jvmVersion: String by project
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(jvmVersion)
        moduleName = "android.kaiteki.compose"
    }
}

dependencies {
    implementation(magic.kotlin.stdlib.jdk8)
    implementation(magic.kotlinx.coroutines.android)
    implementation(magic.kotlinx.immutableCollections)

    implementation(androidx.activity.compose)
    implementation(androidx.browser)
    implementation(androidx.navigation.runtime)
    implementation(androidx.hilt.navigation.compose)

    implementation(magic.arrow.core)

    implementation(platform(androidx.compose.bom))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(project(":core"))

    coreLibraryDesugaring(magic.desugar)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.addAll(
        layout.projectDirectory.file("compose_stability.conf")
    )
}
