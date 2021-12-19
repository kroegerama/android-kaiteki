import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension

@Suppress("UnstableApiUsage")
object BuildConfig {

    fun configurePublish() = Action<Project> {
        val nexusUsername: String? by this
        val nexusPassword: String? by this
        val signingKey: String? by this
        val signingPassword: String? by this

        configure<PublishingExtension> {
            publications {

                create<MavenPublication>("maven") {
                    val sourcesJar = project.task<Jar>("sourcesJar") {
                        archiveClassifier.set("sources")

                        project.the<BaseExtension>().sourceSets.forEach {
                            from(it.java.srcDirs)
                            from((it.kotlin as AndroidSourceDirectorySet).srcDirs)
                        }
                    }
                    from(components["release"])

                    artifact(sourcesJar)
                    pom(createPomAction())
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
    }

    private fun createPomAction() = Action<MavenPom> {
        name.set(P.projectName)
        description.set(P.projectDescription)
        url.set(P.projectUrl)

        licenses(projectLicenses)
        developers(projectDevelopers)
        scm(projectScm)
    }

    private val projectLicenses = Action<MavenPomLicenseSpec> {
        license {
            name.set(P.pomLicense)
            url.set(P.pomLicenseUrl)
        }
    }

    private val projectDevelopers = Action<MavenPomDeveloperSpec> {
        developer {
            id.set("kroegerama")
            name.set("Chris")
            email.set("1519044+kroegerama@users.noreply.github.com")
        }
    }

    private val projectScm = Action<MavenPomScm> {
        url.set(P.projectUrl)
        connection.set(P.projectScm)
        developerConnection.set(P.developerScm)
    }
}