import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.versions)
    alias(libs.plugins.nexus.publish)
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

tasks.withType<DependencyUpdatesTask>().configureEach {
    gradleReleaseChannel = "current"
    revision = "release"
    val nonStableQualifiers = listOf("alpha", "beta", "rc")
    fun isNonStable(version: String): Boolean = nonStableQualifiers.any { qualifier ->
        qualifier in version.lowercase()
    }
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}
