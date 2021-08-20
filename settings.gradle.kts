enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "android-kaiteki"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        val magicCatalogs: String by settings

        create("lib") {
            from("com.kroegerama.magic-catalogs:base:$magicCatalogs")
        }
        create("androidx") {
            from("com.kroegerama.magic-catalogs:androidx:$magicCatalogs")
        }
    }
}

include(":core")
include(":retrofit")
include(":recyclerview")
include(":views")

include(":example")