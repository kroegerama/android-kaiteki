package com.kroegerama.kaiteki.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

@Suppress("UnstableApiUsage")
class EntryPointAnnotationDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf<Class<out UElement>>(
        UClass::class.java
    )

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitClass(node: UClass) {
            if (node.hasModifier(JvmModifier.ABSTRACT)) return
            if (node.hasAnnotation(ANNOTATION)) return
            val supers = InheritanceUtil.getSuperClasses(node.javaPsi).mapNotNull { it.qualifiedName }
            if (FRAGMENT !in supers && ACTIVITY !in supers) return

            val fix = LintFix.create()
                .annotate(ANNOTATION)
                .build()

            val incident = Incident(ISSUE, BRIEF, context.getNameLocation(node), fix)

            context.report(incident)
        }
    }

    companion object {
        private const val ANNOTATION = "dagger.hilt.android.AndroidEntryPoint"
        private const val FRAGMENT = "androidx.fragment.app.Fragment"
        private const val ACTIVITY = "androidx.appcompat.app.AppCompatActivity"

        private const val BRIEF = "Activity/Fragment should have @AndroidEntryPoint Annotation"
        private const val EXPLANATION = "Activity/Fragment should have @AndroidEntryPoint Annotation for Hilt to be able to inject fields."

        val ISSUE = Issue.create(
            id = "AndroidEntryPointAnnotation",
            briefDescription = BRIEF,
            explanation = EXPLANATION,
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(EntryPointAnnotationDetector::class.java, Scope.JAVA_FILE_SCOPE),
            enabledByDefault = false,
            androidSpecific = true
        )
    }
}
