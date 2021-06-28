import BuildConfig.configurePublish

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.mavenPublish)
    id(Plugins.signing)
}

android {
    compileSdk = Android.compileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        moduleName = "android.kaiteki.core"
    }
    buildFeatures {
        buildConfig = false
        viewBinding = true
    }

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(platform(Libs.platform))

    implementation(Libs.kotlinStdLib)
    implementation(Libs.coroutines)

    implementation(Libs.appcompat)
    implementation(Libs.material)

    implementation(Libs.androidxBrowser)
    implementation(Libs.androidxCore)
    implementation(Libs.androidxPreference)
    implementation(Libs.androidxRecycler)
    implementation(Libs.androidxExif)
    implementation(Libs.androidxLifecycleCommon)
    implementation(Libs.androidxLifecycleLiveData)
    implementation(Libs.androidxLifecycleRuntime)
    implementation(Libs.androidxLifecycleViewModel)
    implementation(Libs.androidxNavigationCommon)
    implementation(Libs.androidxNavigationFragment)
}

afterEvaluate(configurePublish())