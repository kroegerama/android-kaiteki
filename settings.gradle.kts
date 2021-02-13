rootProject.name = "android-kaiteki"

pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

include(":core")
include(":retrofit")
include(":recyclerview")
include(":views")

include(":example")