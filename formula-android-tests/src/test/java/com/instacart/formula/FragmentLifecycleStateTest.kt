package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.FragmentKey
import com.instacart.testutils.android.TestKey
import com.instacart.formula.test.TestKeyWithId
import com.instacart.testutils.android.FormulaAndroidInteractor
import com.instacart.testutils.android.NoOpFeatureFactory
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.showFragment
import com.instacart.testutils.android.withFormulaAndroid
import io.reactivex.rxjava3.observers.TestObserver
import kotlinx.coroutines.rx3.asObservable
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleStateTest {

    private fun runTest(continuation: (FormulaAndroidInteractor) -> Unit) {
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> {
                    ActivityStore(
                        fragmentStore = FragmentStore.Builder().build {
                            bind(NoOpFeatureFactory<TestKey>())
                            bind(NoOpFeatureFactory<TestKeyWithId>())
                        }
                    )
                }
            },
        ) {
            continuation(it)
        }
    }

    @Test
    fun `is fragment started`() {
        runTest { interactor ->
            val fragmentKey = TestKey()
            val startedEvents = interactor.startedEvents(fragmentKey)

            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.showFragment(fragmentKey)

            startedEvents.assertValues(false, true)
        }
    }

    @Test
    fun `is fragment resumed`() {
        runTest { interactor ->
            val fragmentKey = TestKey()
            val resumedEvents = interactor.resumedEvents(fragmentKey)
            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.showFragment(fragmentKey)
            resumedEvents.assertValues(false, true)
        }
    }

    @Test
    fun `navigate forward`() {
        runTest { interactor ->
            val initialKey = TestKey()
            val initialKeyStartedEvents = interactor.startedEvents(initialKey)
            val initialKeyResumedEvents = interactor.resumedEvents(initialKey)

            val detailKey = TestKeyWithId(1)
            val detailKeyStartedEvents = interactor.startedEvents(detailKey)
            val detailKeyResumedEvents = interactor.resumedEvents(detailKey)

            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.showFragment(initialKey)
            scenario.showFragment(detailKey)

            initialKeyStartedEvents.assertValues(false, true, false)
            initialKeyResumedEvents.assertValues(false, true, false)

            detailKeyStartedEvents.assertValues(false, true)
            detailKeyResumedEvents.assertValues(false, true)
        }
    }

    private fun FormulaAndroidInteractor.startedEvents(key: FragmentKey): TestObserver<Boolean> {
        return selectEvents { it.isFragmentStarted(key).asObservable() }.test()
    }

    private fun FormulaAndroidInteractor.resumedEvents(key: FragmentKey): TestObserver<Boolean> {
        return selectEvents { it.isFragmentResumed(key).asObservable() }.test()
    }
}
