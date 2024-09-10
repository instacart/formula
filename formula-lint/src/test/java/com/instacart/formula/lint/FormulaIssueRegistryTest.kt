package com.instacart.formula.lint

import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.CURRENT_API
import com.google.common.truth.Truth
import org.junit.Test

@Suppress("CheckResult")
class FormulaIssueRegistryTest {

    @Test fun issues() {
        TestLintTask.lint()

        val issues = FormulaIssueRegistry().issues
        Truth.assertThat(issues).hasSize(2)
    }

    @Test fun apiVersions() {
        TestLintTask.lint()

        Truth.assertThat(FormulaIssueRegistry().api).isEqualTo(CURRENT_API)
        Truth.assertThat(FormulaIssueRegistry().minApi).isEqualTo(7)
    }

    @Test fun vendor() {
        TestLintTask.lint()

        val vendor = FormulaIssueRegistry().vendor
        Truth.assertThat(vendor.vendorName).isEqualTo("Instacart/formula")
        Truth.assertThat(vendor.identifier).isEqualTo("com.instacart.formula:formula:{version}")
        Truth.assertThat(vendor.feedbackUrl).isEqualTo("https://github.com/Instacart/formula/issues")
    }
}