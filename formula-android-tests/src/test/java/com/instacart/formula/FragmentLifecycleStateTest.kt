package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.ActivityStoreContext
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.test.TestKey
import com.instacart.formula.test.TestKeyWithId
import com.instacart.formula.test.TestFragmentActivity
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleStateTest {

    private lateinit var started: MutableList<Pair<FragmentKey, Boolean>>
    private lateinit var resumed: MutableList<Pair<FragmentKey, Boolean>>

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFragmentActivity> {
                    started = mutableListOf()
                    resumed = mutableListOf()

                    store(
                        configureActivity = {
                            initialContract = TestKey()
                        },
                        contracts =  {
                            bind(featureFactory<TestKey>(this@activity))
                            bind(featureFactory<TestKeyWithId>(this@activity))
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
        val events = selectStartedEvents(TestKey())
        assertThat(events).containsExactly(false, true).inOrder()
    }

    @Test fun `is fragment resumed`() {
        val events = selectResumedEvents(TestKey())
        assertThat(events).containsExactly(false, true).inOrder()
    }

    @LooperMode(LooperMode.Mode.LEGACY)
    @Test fun `navigate forward`() {
        navigateToTaskDetail()

        val contract = TestKey()
        val detail = TestKeyWithId(1)

        assertThat(selectStartedEvents(contract)).containsExactly(false, true, false).inOrder()
        assertThat(selectResumedEvents(contract)).containsExactly(false, true, false).inOrder()

        assertThat(selectStartedEvents(detail)).containsExactly(false, true).inOrder()
        assertThat(selectResumedEvents(detail)).containsExactly(false, true).inOrder()
    }

    private fun selectStartedEvents(contract: FragmentKey): List<Boolean> {
        return started.filter { it.first == contract }.map { it.second }
    }

    private fun selectResumedEvents(contract: FragmentKey): List<Boolean> {
        return resumed.filter { it.first == contract }.map { it.second }
    }

    private fun navigateToTaskDetail() {
        val detail = TestKeyWithId(1)
        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .remove(it.supportFragmentManager.findFragmentByTag(TestKey().tag)!!)
                .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun ActivityStoreContext<*>.stateChanges(contract: FragmentKey): Observable<Any> {
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

    private fun <Key : FragmentKey> featureFactory(
        storeContext: ActivityStoreContext<*>
    ): FeatureFactory<Unit, Key> {
        return TestFeatureFactory {
            storeContext.stateChanges(it)
        }
    }
}
