package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class SnapshotTest {

    @Test
    fun `using snapshot outside of evaluation will throw an exception`() {
        // Note: you should never expose snapshot like this! This is only to simplify testing
        val formula = object : StatelessFormula<Unit, Snapshot<Unit, Unit>>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Snapshot<Unit, Unit>> {
                return Evaluation(this)
            }
        }

        val observer = formula.test(isValidationEnabled = false)
        observer.input(Unit)
        val result = runCatching {
            observer.output { this.context.callback { none() } }
        }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished"
        )
    }
}