package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test

class TestFormulaTest {
    @Test fun `assert running count is zero when formula is not running`() {
        TestFormulaRobot()
            .withTestFormula { assertRunningCount(0) }
            .start()
            .withTestFormula { assertRunningCount(1) }
    }

    @Test fun `output throws an exception when no test formula is running`() {
        try {
            TestFormulaRobot()
                .withTestFormula { output(TestFormulaRobot.ChildFormula.Button(onNameChanged = {})) }

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula is not running")
        }
    }

    @Test fun `output emits new output to the parent`() {
        val newOutput = TestFormulaRobot.ChildFormula.Button(onNameChanged = {})
        TestFormulaRobot()
            .start()
            .withTestFormula { output(newOutput) }
            .assertOutput { assertThat(this.button).isEqualTo(newOutput) }
    }

    @Test fun `output with key throws an exception when test formula matching key is not running`() {
        val newOutput = TestFormulaRobot.ChildFormula.Button(onNameChanged = {})
        try {
            TestFormulaRobot()
                .start()
                .withTestFormula {
                    output(key = "random-key", newOutput)
                }

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula for random-key is not running, there are [child-key] running")
        }
    }

    @Test fun `output with key emits new output to the parent when key matches`() {
        val newOutput = TestFormulaRobot.ChildFormula.Button(onNameChanged = {})
        TestFormulaRobot()
            .start()
            .withTestFormula { output(key = "child-key", newOutput) }
            .assertOutput { assertThat(this.button).isEqualTo(newOutput) }
    }

    @Test fun `input() throw an exception when no formulas are running`() {
        try {
            TestFormulaRobot()
                .withTestFormula {
                    input { onChangeName("my name") }
                }

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula is not running")
        }
    }

    @Test fun `input() works as expected when formula is running`() {
        TestFormulaRobot()
            .start()
            .withTestFormula {
                input { onChangeName("my name") }
            }
            .assertOutput {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `input() throws an error when NO formula that matches the key provided is running`() {
        try {
            TestFormulaRobot()
                .start()
                .withTestFormula {
                    input(key = "random-key") { onChangeName("my name") }
                }

            fail("Should not happen")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().startsWith("Formula for random-key is not running, there are [child-key] running")
        }
    }

    @Test fun `input() works as expected when formula that matches the key provided is running`() {
        TestFormulaRobot()
            .start()
            .withTestFormula {
                input(key = "child-key") { onChangeName("my name") }
            }
            .assertOutput {
                assertThat(name).isEqualTo("my name")
            }
    }

    @Test fun `input passed to formula`() {
        TestFormulaRobot()
            .start()
            .withTestFormula {
                input { assertThat(name).isEqualTo("") }
            }
    }
}
