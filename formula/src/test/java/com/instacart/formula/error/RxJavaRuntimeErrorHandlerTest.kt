package com.instacart.formula.error

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.DuplicateKeyException
import com.instacart.formula.internal.ClearPluginsRule
import com.instacart.formula.internal.Try
import com.instacart.formula.rxjava3.RxJavaRuntime
import com.instacart.formula.rxjava3.RxJavaRuntimeErrorHandler
import com.instacart.formula.subjects.DynamicParentFormula
import com.instacart.formula.subjects.OnlyUpdateFormula
import com.instacart.formula.subjects.TestKey
import com.instacart.formula.test.RxJavaTestableRuntime
import com.instacart.formula.test.TestableRuntime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import kotlin.IllegalStateException

class RxJavaRuntimeErrorHandlerTest {

    val runtime: TestableRuntime = RxJavaTestableRuntime

    @get:Rule
    val rule = RuleChain
        .outerRule(TestName())
        .around(ClearPluginsRule())
        .around(runtime.rule)

    private val errorLogs = mutableListOf<String>()

    private val duplicateKeyErrorHandler = object : RxJavaRuntimeErrorHandler {
        override fun onError(error: Throwable): Boolean {
            return when (error) {
                is DuplicateKeyException -> {
                    errorLogs.add(error.message.orEmpty())
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    @Before
    fun setUp() {
        RxJavaRuntime.setDefaultErrorHandler(duplicateKeyErrorHandler)
    }

    @After
    fun tearDown() {
        RxJavaRuntime.setDefaultErrorHandler(null)
    }

    @Test
    fun `emitting a generic error throws an exception`() {
        val result = Try {
            val formula = OnlyUpdateFormula<Unit> {
                events(Action.onInit()) {
                    throw IllegalStateException("crashed")
                }
            }
            runtime.test(formula, Unit)
        }

        val error = result.errorOrNull()?.cause
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
        assertThat(error?.message).isEqualTo("crashed")

        assertThat(errorLogs).isEmpty()
    }

    @Test
    fun `adding duplicate child logs an exception`() {
        val result = Try {
            val formula = DynamicParentFormula()
            runtime.test(formula, Unit)
                .output { addChild(TestKey("1")) }
                .output { addChild(TestKey("1")) }
        }

        val error = result.errorOrNull()?.cause
        assertThat(error).isNull()
        assertThat(errorLogs).hasSize(1)

        val log = errorLogs.first()
        val expectedLog = "There already is a child with same key: FormulaKey(scopeKey=null, type=class com.instacart.formula.subjects.KeyFormula, key=TestKey(id=1)). Override [Formula.key] function."
        assertThat(log).isEqualTo(expectedLog)
    }
}
