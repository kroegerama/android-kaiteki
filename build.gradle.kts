import io.github.gradlenexus.publishplugin.NexusPublishExtension

plugins {
    id(Plugins.androidLibrary) apply false
    id(Plugins.androidApplication) apply false
    kotlin("android") apply false
    id(Plugins.nexusPublish)
}

allprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.jetbrains.trove4j:trove4j:20160824")).using(module("org.jetbrains.intellij.deps:trove4j:1.0.20181211"))
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    version = P.projectVersion
    group = P.projectGroupId
    description = P.projectDescription
}

val clean by tasks.creating(Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}

configure<NexusPublishExtension> {
    val nexusStagingProfileId: String? by project
    val nexusUsername: String? by project
    val nexusPassword: String? by project

    packageGroup.set(group.toString())

    repositories {
        sonatype {
            stagingProfileId.set(nexusStagingProfileId)
            username.set(nexusUsername)
            password.set(nexusPassword)
        }
    }
}
