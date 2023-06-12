import BuildConfig.createPomAction

plugins {
    id(Plugins.nexusPublish)
    id("signing")
    id("maven-publish")
}

allprojects {
    version = P.projectVersion
    group = P.projectGroupId
    description = P.projectDescription
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

val nexusStagingProfileId: String? by project
val nexusUsername: String? by project
val nexusPassword: String? by project
val signingKey: String? by project
val signingPassword: String? by project

subprojects {
    if ("example" in name || "lint" in name) return@subprojects
    apply {
        plugin("maven-publish")
        plugin("signing")
    }

    afterEvaluate {
        publishing {
            publications {
                register<MavenPublication>("release") {
                    afterEvaluate {
                        from(components["release"])
                    }
                    pom(createPomAction())
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
}

nexusPublishing {
    packageGroup.set(group.toString())
    repositories(Action {
        sonatype {
            stagingProfileId.set(nexusStagingProfileId)
            username.set(nexusUsername)
            password.set(nexusPassword)

            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    })
}
