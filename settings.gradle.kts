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
include(":retrofit")
include(":recyclerview")
include(":views")
include(":lint")

include(":example")