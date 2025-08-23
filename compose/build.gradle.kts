import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kaiteki.publishing-conventions")
}

android {
    compileSdk = Android.COMPILE_SDK
    namespace = "com.kroegerama.kaiteki.compose"

    buildFeatures {
        buildConfig = false
        compose = true
    }

    defaultConfig {
        minSdk = Android.MIN_SDK
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    testOptions {
        targetSdk = Android.TARGET_SDK
    }
    lint {
        targetSdk = Android.TARGET_SDK
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        moduleName = "android.kaiteki.compose"
        jvmTarget = JvmTarget.JVM_11
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
    coreLibrariesVersion = "2.0.21"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.activity.compose)
    implementation(libs.browser)
    implementation(libs.navigation.runtime)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.arrow.core)

    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(project(":core"))

    coreLibraryDesugaring(libs.desugar)
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.addAll(
        layout.projectDirectory.file("compose_stability.conf")
    )
}
