package com.kroegerama.kaiteki.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class LintRegistry : IssueRegistry() {

    override val api: Int = CURRENT_API

    override val issues: List<Issue> = listOf(
        //source checks
        EntryPointAnnotationDetector.ISSUE,
        ViewModelAnnotationDetector.ISSUE,

        //layout checks
        ButtonDetector.ISSUE,
        RecyclerViewDetector.ISSUE,
        ScrollViewDetector.ISSUE
    )

    override val minApi: Int = 11

    override val vendor: Vendor = Vendor("kroegerama")
}
