import BuildConfig.createPomAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.lint)
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.lint.api)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.release = 11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xjdk-release=11")
    }
}

val signingKey: String? by project
val signingPassword: String? by project

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom(createPomAction())
            from(components["java"])
        }
    }
}

signing {
    sign(extensions.getByType<PublishingExtension>().publications)
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
