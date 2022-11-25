package com.kroegerama.kaiteki.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

@Suppress("UnstableApiUsage")
class ViewModelAnnotationDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes() = listOf<Class<out UElement>>(
        UClass::class.java
    )

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitClass(node: UClass) {
            if (node.hasModifier(JvmModifier.ABSTRACT)) return
            val supers = InheritanceUtil.getSuperClasses(node.javaPsi).mapNotNull { it.qualifiedName }
            if (VIEW_MODEL !in supers) return

            val incidents = mutableListOf<Incident>()

            if (!node.hasAnnotation(ANNOTATION_HILT_VIEW_MODEL)) {
                incidents += Incident(
                    ISSUE,
                    BRIEF,
                    context.getNameLocation(node),
                    LintFix.create()
                        .annotate(ANNOTATION_HILT_VIEW_MODEL)
                        .build()
                )
            }

            node.constructors.forEach { constructor ->
                if (!constructor.hasAnnotation(ANNOTATION_INJECT)) {
                    incidents += Incident(
                        ISSUE,
                        BRIEF,
                        context.getLocation(constructor),
                        LintFix.create()
                            .annotate(ANNOTATION_INJECT)
                            .build()
                    )
                }
            }

            incidents.forEach(context::report)
        }
    }

    companion object {
        private const val VIEW_MODEL = "androidx.lifecycle.ViewModel"
        private const val ANNOTATION_HILT_VIEW_MODEL = "dagger.hilt.android.lifecycle.HiltViewModel"
        private const val ANNOTATION_INJECT = "javax.inject.Inject"

        private const val BRIEF = "ViewModel should have @HiltViewModel Annotation"
        private const val EXPLANATION = "ViewModel should have @HiltViewModel Annotation for Hilt to be able to inject fields."

        val ISSUE = Issue.create(
            id = "HiltViewModelAnnotation",
            briefDescription = BRIEF,
            explanation = EXPLANATION,
            category = Category.CUSTOM_LINT_CHECKS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(ViewModelAnnotationDetector::class.java, Scope.JAVA_FILE_SCOPE),
            enabledByDefault = false,
            androidSpecific = true
        )
    }
}
