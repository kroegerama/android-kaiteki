import BuildConfig.createPomAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(magic.plugins.kotlin.jvm)
    id("com.android.lint")
    `maven-publish`
    signing
}

dependencies {
    compileOnly(magic.kotlin.stdlib.jdk8)
    compileOnly("com.android.tools.lint:lint-api:31.7.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    val jvmVersion: String by project
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(jvmVersion)
    }
}

val nexusUsername: String? by project
val nexusPassword: String? by project
val signingKey: String? by project
val signingPassword: String? by project

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            pom(createPomAction())
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}

configure<SigningExtension> {
    sign(extensions.getByType<PublishingExtension>().publications)
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
