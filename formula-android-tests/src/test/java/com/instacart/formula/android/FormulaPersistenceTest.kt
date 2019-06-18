package com.instacart.formula.android

import android.os.Parcelable
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.TestFormulaRule
import com.instacart.formula.integration.FormulaAppCompatActivity
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.android.parcel.Parcelize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormulaPersistenceTest {

    class TestActivity : FormulaAppCompatActivity()

    @Parcelize
    data class MyState(val value: String) : Parcelable

    private var restoredState: MyState? = null
    private lateinit var valueRelay: PublishRelay<Int>

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            valueRelay = PublishRelay.create()

            FormulaAndroid.init(app) {
                activity<TestActivity> {
                    val myPersistedState = withState<MyState>()
                    restoredState = myPersistedState.current()

                    store(
                        streams = {
                            valueRelay.subscribe { myPersistedState.save(MyState(value = "$it")) }
                        },
                        contracts = {
                        }
                    )
                }
            }
        })

    private val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `process death`() {
        valueRelay.accept(100)

        formulaRule.fakeProcessDeath()

        assertThat(restoredState).isEqualTo(MyState("100"))
    }
}
