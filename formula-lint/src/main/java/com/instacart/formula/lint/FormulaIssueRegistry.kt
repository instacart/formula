package com.instacart.formula.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.google.auto.service.AutoService
import com.instacart.formula.lint.WrongFormulaUsageDetector

@Suppress("UnstableApiUsage", "unused")
@AutoService(value = [IssueRegistry::class])
class FormulaIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = WrongFormulaUsageDetector.issues.asList()

    override val api: Int
        get() = CURRENT_API

    /**
     * works with Studio 4.0 or later; see
     * [com.android.tools.lint.detector.api.describeApi]
     */
    override val minApi: Int
        get() = 7

    override val vendor = Vendor(
        vendorName = "Instacart/formula",
        identifier = "com.instacart.formula:formula:{version}",
        feedbackUrl = "https://github.com/Instacart/formula/issues",
    )
}