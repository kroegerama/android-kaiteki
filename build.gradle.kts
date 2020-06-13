plugins {
    id(Plugins.androidLibrary) apply false
    id(Plugins.androidApplication) apply false
    kotlin("android") apply false
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    version = P.projectVersion
    group = P.projectGroupId
    description = P.projectDescription
}

val clean by tasks.creating(Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}