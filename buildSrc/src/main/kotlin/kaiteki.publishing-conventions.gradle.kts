import BuildConfig.createPomAction

plugins {
    `maven-publish`
    `signing`
}

val signingKey: String? by project
val signingPassword: String? by project

publishing {
    publications {
        register<MavenPublication>("release") {
            pom(createPomAction())
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

signing {
    sign(publishing.publications)
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
