enableFeaturePreview("VERSION_CATALOGS")

file("../gradle.properties").inputStream().use { input ->
    java.util.Properties().apply { load(input) }
}.run {
    stringPropertyNames().forEach { k ->
        extra.set(k, getProperty(k))
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        val magicCatalogs: String by settings

        create("lib") {
            from("com.kroegerama.magic-catalogs:base:$magicCatalogs")
        }
        create("plg") {
            from("com.kroegerama.magic-catalogs:plugins:$magicCatalogs")
        }
    }
}
