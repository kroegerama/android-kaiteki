import org.gradle.api.Action
import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*

@Suppress("UnstableApiUsage")
object BuildConfig {

    fun createPomAction() = Action<MavenPom> {
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
