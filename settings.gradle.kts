pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "android-kaiteki"

include(":core")
include(":compose")
include(":retrofit")
include(":recyclerview")
include(":paging")
include(":views")
include(":lint")

include(":example")