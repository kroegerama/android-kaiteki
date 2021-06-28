plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
}

android {
    compileSdk = Android.compileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform(Libs.platform))

    implementation(Libs.kotlinStdLib)

    implementation(Libs.appcompat)
    implementation(Libs.material)

    implementation(Libs.androidxCore)
    implementation(Libs.androidxConstraint)
    implementation(Libs.androidxFragment)
    implementation(Libs.androidxLifecycleViewModel)
    implementation(Libs.androidxLifecycleLiveData)
    implementation(Libs.androidxLifecycleRuntime)

    implementation(project(":core"))
    implementation(project(":recyclerview"))
    implementation(project(":retrofit"))
    implementation(project(":views"))

    debugImplementation(Libs.leakCanary)
}
