import com.android.build.gradle.BaseExtension
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

@Suppress("UnstableApiUsage")
object BuildConfig {

    fun configurePublish() = Action<Project> {
        configure<PublishingExtension> {
            publications {

                create<MavenPublication>("maven") {
                    val sourcesJar = project.task<Jar>("sourcesJar") {
                        archiveClassifier.set("sources")
                        from(project.the<BaseExtension>().sourceSets["main"].java.srcDirs)
                    }
                    from(components["release"])

                    artifact(sourcesJar)
                    pom(createPomAction())
                }
            }
        }

        configure<BintrayExtension> {
            user = project.findProperty("bintrayUser") as? String
            key = project.findProperty("bintrayApiKey") as? String

            setPublications("maven")

            pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
                repo = "maven"
                userOrg = "kroegerama"

                name = "${rootProject.name}:${project.name}"
                desc = project.description

                setLicenses(P.pkgLicense)
                setLabels(*P.projectLabels)
                vcsUrl = P.projectUrl
                githubRepo = P.githubRepo

                publicDownloadNumbers = true
            })
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