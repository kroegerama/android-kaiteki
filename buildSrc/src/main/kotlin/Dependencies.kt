object V {
    const val coroutines = "1.3.3"
    const val core = "1.2.0"
    const val appcompat = "1.1.0"
    const val constraint = "2.0.0-beta4"
    const val material = "1.1.0"
    const val lifecycle = "2.2.0"
    const val preference = "1.1.0"
    const val recycler = "1.1.0"
    const val exif = "1.1.0"
    const val paging = "2.1.1"

    const val retrofit = "2.7.1"
}

object Plugins {
    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExt = "kotlin-android-extensions"
    const val mavenPublish = "maven-publish"
    const val bintray = "com.jfrog.bintray"
}

object Android {
    const val minSdk = 17
    const val compileSdk = 29
    const val targetSdk = 29
}

object Libs {
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${V.coroutines}"

    const val appcompat = "androidx.appcompat:appcompat:${V.appcompat}"
    const val material = "com.google.android.material:material:${V.material}"

    const val androidxCore = "androidx.core:core-ktx:${V.core}"
    const val androidxConstraint = "androidx.constraintlayout:constraintlayout:${V.constraint}"
    const val androidxLifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${V.lifecycle}"
    const val androidxLifecycleExt = "androidx.lifecycle:lifecycle-extensions:${V.lifecycle}"
    const val androidxLifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${V.lifecycle}"
    const val androidxLifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${V.lifecycle}"
    const val androidxPreference = "androidx.preference:preference:${V.preference}"
    const val androidxRecycler = "androidx.recyclerview:recyclerview:${V.recycler}"
    const val androidxExif = "androidx.exifinterface:exifinterface:${V.exif}"
    const val androidxPaging = "androidx.paging:paging-runtime-ktx:${V.paging}"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.1"

    const val retrofit = "com.squareup.retrofit2:retrofit:${V.retrofit}"
    const val diskLru = "com.jakewharton:disklrucache:2.0.2"
}