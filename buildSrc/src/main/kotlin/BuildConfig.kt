import com.android.build.gradle.BaseExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.the
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
                        }
//                        from(project.the<BaseExtension>().sourceSets["main"].java.srcDirs)
                    }
                    from(components["release"])

                    artifact(sourcesJar)
                    pom(createPomAction())
                }
            }
            repositories {
                maven {
                    name = "sonatype"
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }

        configure<SigningExtension> {
            sign(convention.getByType<PublishingExtension>().publications)
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