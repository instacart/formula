package com.instacart.formula

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.integration.FormulaAppCompatActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityLifecycleEventTest {

    class TestActivity : FormulaAppCompatActivity()

    private lateinit var events: MutableList<Lifecycle.Event>

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestActivity> {
                    events = mutableListOf()
                    store(
                        streams = {
                            lifecycleEvents().subscribe {
                                events.add(it)
                            }
                        },
                        contracts = {}
                    )
                }
            }
        })

    private val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule
    val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `full lifecycle`() {
        scenario.recreate()
        scenario.close()

        val lifecycle = listOf(ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY)
        // We expect two full lifecycles
        val expected = lifecycle + lifecycle
        assertThat(events).containsExactlyElementsIn(expected)
    }
}
