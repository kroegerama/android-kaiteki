pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val agp: String by settings
        id("com.android.application") version agp
        id("com.android.library") version agp
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        val magicCatalogs: String by settings

        create("magic") {
            from("com.kroegerama.magic-catalogs:magic:$magicCatalogs")
        }
        create("androidx") {
            from("com.kroegerama.magic-catalogs:androidx:$magicCatalogs")
        }
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