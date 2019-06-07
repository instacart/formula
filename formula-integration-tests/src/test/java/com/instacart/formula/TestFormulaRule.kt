package com.instacart.formula

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.reflect.KClass

/**
 * A rule to initialize FormulaAndroid.
 */
class TestFormulaRule<A : FragmentActivity>(
    private val type : KClass<A>
) : TestWatcher() {
    var lastState: FragmentFlowState? = null
    private val stateChangeRelay = PublishRelay.create<Pair<FragmentContract<*>, Any>>()

    override fun starting(description: Description?) {
        super.starting(description)
        initializeFormula()
    }

    override fun finished(description: Description?) {
        FormulaAndroid.tearDown()
        lastState = null
    }

    fun fakeProcessDeath() {
        FormulaAndroid.tearDown()
        lastState = null
        initializeFormula()
    }

    fun <T : Any> sendStateUpdate(contract: FragmentContract<T>, update: T) {
        stateChangeRelay.accept(Pair<FragmentContract<*>, Any>(contract, update))
    }

    private fun initializeFormula() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        FormulaAndroid.init(context) {
            activity(type) {
                store(
                    onRenderFragmentState = { a, state ->
                        lastState = state
                    }
                ) {
                    bind { key: TaskListContract ->
                        stateChanges(key)
                    }

                    bind { key: TaskDetailContract ->
                        stateChanges(key)
                    }

                    bind(TestLifecycleContract::class, ::stateChanges)
                }
            }

        }
    }

    private fun stateChanges(contract: FragmentContract<*>): Observable<Any> {
        return stateChangeRelay
            .filter { event ->
                event.first == contract
            }
            .map { it.second }
    }
}
