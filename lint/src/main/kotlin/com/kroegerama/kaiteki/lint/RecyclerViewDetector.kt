package com.kroegerama.kaiteki.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

class RecyclerViewDetector : LayoutDetector() {

    override fun getApplicableElements(): Collection<String> = listOf(
        "androidx.recyclerview.widget.RecyclerView"
    )

    override fun visitElement(context: XmlContext, element: Element) {
        if (element.hasAttributeNS(SdkConstants.AUTO_URI, ATTR_LAYOUT_MANAGER)) return

        val fix = LintFix.create()
            .set(SdkConstants.AUTO_URI, ATTR_LAYOUT_MANAGER, ATTR_LAYOUT_MANAGER_FIX)
            .caret(ATTR_LAYOUT_MANAGER_FIX.length)
            .build()

        val incident = Incident(ISSUE, BRIEF, context.getNameLocation(element), fix)

        context.report(incident)
    }

    companion object {
        private const val ATTR_LAYOUT_MANAGER_FIX = "androidx.recyclerview.widget.LinearLayoutManager"
        private const val ATTR_LAYOUT_MANAGER = "layoutManager"
        private const val BRIEF = "RecyclerView is missing the app:layoutManager attribute"
        private const val EXPLANATION = "If you are not setting a layoutManager in code, this RecyclerView will stay empty"

        val ISSUE = Issue.create(
            id = "RecyclerViewLayoutManager",
            briefDescription = BRIEF,
            explanation = EXPLANATION,
            implementation = Implementation(RecyclerViewDetector::class.java, Scope.RESOURCE_FILE_SCOPE),
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
