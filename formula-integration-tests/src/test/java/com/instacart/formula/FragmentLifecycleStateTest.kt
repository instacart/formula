package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentLifecycleState
import com.instacart.formula.integration.ActivityStoreContext
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleStateTest {

    private lateinit var lifecycleEvents: MutableList<Pair<FragmentContract<*>, FragmentLifecycleState>>

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFragmentActivity> {
                    lifecycleEvents = mutableListOf()

                    store(
                        configureActivity = {
                            initialContract = TestContract()
                        },
                        contracts = {
                            bind { key: TestContract ->
                                stateChanges(key)
                            }

                            bind { key: TestContractWithId ->
                                stateChanges(key)
                            }
                        }
                    )
                }

            }
        })

    private val activityRule = ActivityScenarioRule(TestFragmentActivity::class.java)

    @get:Rule
    val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

    @Before
    fun setup() {
        scenario = activityRule.scenario
    }

    @Test
    fun `initial lifecycle`() {
        val events = selectEvents(TestContract())
        assertThat(events).containsExactly(
            FragmentLifecycleState.CREATED,
            FragmentLifecycleState.STARTED,
            FragmentLifecycleState.RESUMED
        )
    }

    @Test
    fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        val contract = TestContract()
        val detail = TestContractWithId(1)

        val firstScreenEvents = selectEvents(contract)
        assertThat(firstScreenEvents).containsExactly(
            FragmentLifecycleState.CREATED,
            FragmentLifecycleState.STARTED,
            FragmentLifecycleState.RESUMED,
            FragmentLifecycleState.PAUSED,
            FragmentLifecycleState.STOPPED,
            FragmentLifecycleState.DESTROYED
        )

        val detailEvents = selectEvents(detail)
        assertThat(detailEvents).containsExactly(
            FragmentLifecycleState.CREATED,
            FragmentLifecycleState.STARTED,
            FragmentLifecycleState.RESUMED
        )
    }

    private fun selectEvents(contract: FragmentContract<*>) =
        lifecycleEvents.filter { it.first == contract }.map { it.second }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail() {
        val detail = TestContractWithId(1)
        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .remove(it.supportFragmentManager.findFragmentByTag(TestContract().tag)!!)
                .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun activity(): TestFragmentActivity {
        return scenario.activity()
    }

    private fun ActivityStoreContext<*>.stateChanges(contract: FragmentContract<*>): Observable<Any> {
        return fragmentLifecycleState(contract).flatMap {
            lifecycleEvents.add(contract to it)
            Observable.empty<Any>()
        }
    }
}
