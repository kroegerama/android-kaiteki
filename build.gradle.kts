import BuildConfig.createPomAction

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.nexus.publish)
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

val sonatypeUsername: String? by project
val sonatypePassword: String? by project
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
    packageGroup = group.toString()
    repositories {
        sonatype {
            username = sonatypeUsername
            password = sonatypePassword

            nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
            snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}
