package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.test.TestKey
import com.instacart.formula.test.TestKeyWithId
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.testutils.android.FormulaAndroidInteractor
import com.instacart.testutils.android.NoOpFeatureFactory
import com.instacart.testutils.android.withFormulaAndroid
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import com.instacart.testutils.android.R as TestR

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleStateTest {

    private fun runTest(continuation: (FormulaAndroidInteractor) -> Unit) {
        withFormulaAndroid(
            configure = {
                activity<TestFragmentActivity> {
                    ActivityStore(
                        configureActivity = {
                            it.initialKey = TestKey()
                        },
                        fragmentStore = FragmentStore.init {
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
            val startedEvents = interactor.startedEvents(TestKey())
            ActivityScenario.launch(TestFragmentActivity::class.java)
            startedEvents.assertValues(false, true)
        }
    }

    @Test
    fun `is fragment resumed`() {
        runTest { interactor ->
            val resumedEvents = interactor.resumedEvents(TestKey())
            ActivityScenario.launch(TestFragmentActivity::class.java)
            resumedEvents.assertValues(false, true)
        }
    }

    @LooperMode(LooperMode.Mode.LEGACY)
    @Test
    fun `navigate forward`() {
        runTest { interactor ->
            val initialKeyStartedEvents = interactor.startedEvents(TestKey())
            val initialKeyResumedEvents = interactor.resumedEvents(TestKey())

            val detailKey = TestKeyWithId(1)
            val detailKeyStartedEvents = interactor.startedEvents(detailKey)
            val detailKeyResumedEvents = interactor.resumedEvents(detailKey)

            val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
            navigateToTaskDetail(scenario, detailKey)

            initialKeyStartedEvents.assertValues(false, true, false)
            initialKeyResumedEvents.assertValues(false, true, false)

            detailKeyStartedEvents.assertValues(false, true)
            detailKeyResumedEvents.assertValues(false, true)
        }
    }

    private fun FormulaAndroidInteractor.startedEvents(key: FragmentKey): TestObserver<Boolean> {
        return selectEvents { it.isFragmentStarted(key) }.test()
    }

    private fun FormulaAndroidInteractor.resumedEvents(key: FragmentKey): TestObserver<Boolean> {
        return selectEvents { it.isFragmentResumed(key) }.test()
    }

    private fun navigateToTaskDetail(
        scenario: ActivityScenario<TestFragmentActivity>,
        key: TestKeyWithId,
    ) {
        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .remove(it.supportFragmentManager.findFragmentByTag(TestKey().tag)!!)
                .add(TestR.id.activity_content, FormulaFragment.newInstance(key), key.tag)
                .addToBackStack(null)
                .commit()
        }
    }
}
