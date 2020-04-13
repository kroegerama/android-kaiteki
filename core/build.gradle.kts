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
        freeCompilerArgs = listOf("-module-name", "android.kaiteki.core")
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
}

dependencies {
    implementation(Libs.kotlinStdLib)
    implementation(Libs.coroutines)

    implementation(Libs.appcompat)
    implementation(Libs.material)

    implementation(Libs.androidxCore)
    implementation(Libs.androidxPreference)
    implementation(Libs.androidxRecycler)
    implementation(Libs.androidxExif)
    implementation(Libs.androidxLifecycleCommon)
    implementation(Libs.androidxLifecycleLiveData)
    implementation(Libs.androidxViewBinding)
}

afterEvaluate(configurePublish())