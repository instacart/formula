package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.android.ActivityStoreContext
import com.instacart.formula.test.TestContract
import com.instacart.formula.test.TestContractWithId
import com.instacart.formula.test.TestFragmentActivity
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleStateTest {

    private lateinit var started: MutableList<Pair<FragmentContract<*>, Boolean>>
    private lateinit var resumed: MutableList<Pair<FragmentContract<*>, Boolean>>

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFragmentActivity> {
                    started = mutableListOf()
                    resumed = mutableListOf()

                    store(
                        configureActivity = {
                            initialContract = TestContract()
                        },
                        contracts =  {
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

    @Test fun `is fragment started`() {
        val events = selectStartedEvents(TestContract())
        assertThat(events).containsExactly(false, true).inOrder()
    }

    @Test fun `is fragment resumed`() {
        val events = selectResumedEvents(TestContract())
        assertThat(events).containsExactly(false, true).inOrder()
    }

    @Test fun `navigate forward`() {
        navigateToTaskDetail()

        val contract = TestContract()
        val detail = TestContractWithId(1)

        assertThat(selectStartedEvents(contract)).containsExactly(false, true, false).inOrder()
        assertThat(selectResumedEvents(contract)).containsExactly(false, true, false).inOrder()

        assertThat(selectStartedEvents(detail)).containsExactly(false, true).inOrder()
        assertThat(selectResumedEvents(detail)).containsExactly(false, true).inOrder()
    }

    private fun selectStartedEvents(contract: FragmentContract<*>): List<Boolean> {
        return started.filter { it.first == contract }.map { it.second }
    }

    private fun selectResumedEvents(contract: FragmentContract<*>): List<Boolean> {
        return resumed.filter { it.first == contract }.map { it.second }
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

    private fun ActivityStoreContext<*>.stateChanges(contract: FragmentContract<*>): Observable<Any> {
        val started = isFragmentStarted(contract).flatMap {
            started.add(contract to it)
            Observable.empty<Any>()
        }

        val resumed = isFragmentResumed(contract).flatMap {
            resumed.add(contract to it)
            Observable.empty<Any>()
        }

        return started.mergeWith(resumed)
    }
}
