object Plugins {
    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val mavenPublish = "maven-publish"
    const val signing = "signing"
    const val nexusPublish = "io.github.gradle-nexus.publish-plugin"
}

object Android {
    const val minSdk = 21
    const val compileSdk = 30
    const val targetSdk = 30
}

object Libs {
    const val platform = "com.kroegerama:magic-bom:1.1.4"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core"

    const val appcompat = "androidx.appcompat:appcompat"
    const val material = "com.google.android.material:material"

    const val androidxCore = "androidx.core:core-ktx"
    const val androidxConstraint = "androidx.constraintlayout:constraintlayout"
    const val androidxFragment = "androidx.fragment:fragment-ktx"
    const val androidxLifecycleCommon = "androidx.lifecycle:lifecycle-common-java8"
    const val androidxLifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx"
    const val androidxLifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx"
    const val androidxLifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx"
    const val androidxPreference = "androidx.preference:preference"
    const val androidxRecycler = "androidx.recyclerview:recyclerview"
    const val androidxExif = "androidx.exifinterface:exifinterface"
    const val androidxPaging = "androidx.paging:paging-runtime-ktx"
    const val androidxNavigationCommon = "androidx.navigation:navigation-common"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.5"

    const val retrofit = "com.squareup.retrofit2:retrofit"
    const val moshi = "com.squareup.moshi:moshi"
    const val timber = "com.jakewharton.timber:timber"
}