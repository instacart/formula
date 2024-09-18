package com.instacart.formula

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.activity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityLifecycleEventTest {
    private lateinit var events: MutableList<Lifecycle.State>

    @get:Rule
    val rule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFormulaActivity> {
                    events = mutableListOf()
                    ActivityStore(
                        streams = {
                            activityLifecycleState().subscribe {
                                events.add(it)
                            }
                        }
                    )
                }
            }
        })

    @Test
    fun `full lifecycle`() {
        val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
        scenario.recreate()
        scenario.close()

        val lifecycle = listOf(CREATED, STARTED, RESUMED, STARTED, CREATED, DESTROYED)
        // We expect two full lifecycles
        val expected = listOf(INITIALIZED) + lifecycle + lifecycle
        assertThat(events).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `calling onPreCreate() twice will throw an exception`() {
        val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
        val activity = scenario.activity()
        val result = runCatching { FormulaAndroid.onPreCreate(activity, null) }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "Activity TestFormulaActivity was already initialized. Did you call FormulaAndroid.onPreCreate() twice?"
        )
    }
}
