import BuildConfig.configurePublish

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.mavenPublish)
    id(Plugins.bintray)
}

android {
    compileSdkVersion(Android.compileSdk)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-module-name", "android.kaiteki.recyclerview")
    }
    buildFeatures {
        buildConfig = false
    }

    defaultConfig {
        minSdkVersion(Android.minSdk)
        targetSdkVersion(Android.targetSdk)
        versionCode = 1
        versionName = P.projectVersion
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
}

dependencies {
    implementation(platform(Libs.platform))

    implementation(Libs.kotlinStdLib)

    implementation(Libs.appcompat)

    implementation(Libs.androidxCore)
    implementation(Libs.androidxRecycler)
    implementation(Libs.androidxPaging)
//    implementation(Libs.androidxViewBinding)
}

afterEvaluate(configurePublish())
