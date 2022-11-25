import BuildConfig.createPomAction

plugins {
    `java-library`
    kotlin
    com.android.lint
    `maven-publish`
    signing
}

dependencies {
    compileOnly(lib.kotlin)
    compileOnly("com.android.tools.lint:lint-api:30.3.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
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
