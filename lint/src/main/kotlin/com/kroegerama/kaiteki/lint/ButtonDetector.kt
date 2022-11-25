package com.kroegerama.kaiteki.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

class ButtonDetector : LayoutDetector() {

    override fun getApplicableElements(): Collection<String> = listOf(
        "com.google.android.material.button.MaterialButton"
    )

    override fun visitElement(context: XmlContext, element: Element) {
        if (!element.hasAttributeNS(SdkConstants.ANDROID_URI, ATTR_BACKGROUND)) return

        val fix = LintFix.create()
            .set(SdkConstants.ANDROID_URI, ATTR_BACKGROUND, null)
            .caret(0)
            .build()

        val incident = Incident(ISSUE, BRIEF, context.getLocation(element.getAttributeNodeNS(SdkConstants.ANDROID_URI, ATTR_BACKGROUND)), fix)

        context.report(incident)
    }

    companion object {
        private const val ATTR_BACKGROUND = "background"
        private const val BRIEF = "MaterialButton should not have a android:background attribute"
        private const val EXPLANATION = "You should use materialThemeOverlay and backgroundTint"

        val ISSUE = Issue.create(
            id = "MaterialButtonBackground",
            briefDescription = BRIEF,
            explanation = EXPLANATION,
            implementation = Implementation(ButtonDetector::class.java, Scope.RESOURCE_FILE_SCOPE),
            moreInfo = null,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            enabledByDefault = true,
            androidSpecific = true,
            platforms = Platform.ANDROID_SET,
            suppressAnnotations = null
        )
    }
}
