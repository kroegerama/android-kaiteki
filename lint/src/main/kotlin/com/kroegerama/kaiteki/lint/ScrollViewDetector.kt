package com.kroegerama.kaiteki.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

class ScrollViewDetector : LayoutDetector() {

    override fun getApplicableElements(): Collection<String> = listOf(
        "androidx.core.widget.NestedScrollView"
    )

    override fun visitElement(context: XmlContext, element: Element) {
        if (element.hasAttributeNS(SdkConstants.ANDROID_URI, "id")) return

        val fixValue = "@+id/scrollView"
        val fix = LintFix.create()
            .set(SdkConstants.ANDROID_URI, "id", fixValue)
            .caret(fixValue.length)
            .build()

        val incident = Incident(ISSUE, BRIEF, context.getElementLocation(element), fix)

        context.report(incident)
    }

    companion object {
        private const val BRIEF = "ScrollView should have an android:id"
        private const val EXPLANATION = "To restore the position on config changes, ScrollViews need an id."

        val ISSUE = Issue.create(
            id = "ScrollViewNeedsID",
            briefDescription = BRIEF,
            explanation = EXPLANATION,
            implementation = Implementation(ScrollViewDetector::class.java, Scope.RESOURCE_FILE_SCOPE),
            moreInfo = null,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            enabledByDefault = true,
            androidSpecific = true,
            platforms = Platform.ANDROID_SET,
            suppressAnnotations = null
        )
    }
}
