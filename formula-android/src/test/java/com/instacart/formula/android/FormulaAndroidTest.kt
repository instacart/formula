package com.instacart.formula.android

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.test.runActivityUpdateTest
import com.instacart.testutils.android.TestActivity
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.TestFragmentActivity
import com.instacart.testutils.android.activity
import com.instacart.testutils.android.withFormulaAndroid
import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormulaAndroidTest {

    @Test
    fun `crashes if initialized twice`() {

        try {
            val result = runCatching {
                val context = ApplicationProvider.getApplicationContext<Application>()
                FormulaAndroid.init(context) {}
                FormulaAndroid.init(context) {}
            }
            val error = result.exceptionOrNull()?.message
            Truth.assertThat(error).isEqualTo("can only initialize the store once.")
        } finally {
            FormulaAndroid.reset()
        }
    }

    @Test
    fun `crashes if accessed before initialization`() {
        val result = runCatching {
            FormulaAndroid.onBackPressed(TestFormulaActivity())
        }
        val errorMessage = result.exceptionOrNull()?.message
        Truth.assertThat(errorMessage).isEqualTo(
            "Need to call FormulaAndroid.init() from your Application."
        )
    }

    @Test
    fun `calling onPreCreate() twice will throw an exception`() {
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> {
                    ActivityStore()
                }
            }
        ) {
            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            val activity = scenario.activity()
            val result = runCatching { FormulaAndroid.onPreCreate(activity, null) }
            assertThat(result.exceptionOrNull()).hasMessageThat().contains(
                "Activity TestFormulaActivity was already initialized. Did you call FormulaAndroid.onPreCreate() twice?"
            )
        }
    }

    /**
     * Checks that we handle non-bound activities gracefully.
     */
    @Test
    fun `does not crash when non bound activity is run`() {
        withFormulaAndroid {
            val scenario = ActivityScenario.launch(TestActivity::class.java)
            scenario.recreate()
            scenario.close()
        }
    }

    /**
     * Checks that we handle non-bound fragment activities gracefully.
     */
    @Test
    fun `does not crash when non bound fragment activity is run`() {
        withFormulaAndroid {
            val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
            scenario.recreate()
            scenario.close()
        }
    }

    @Test
    fun `activity lifecycle state emits all events`() {
        var events: MutableList<Lifecycle.State> = mutableListOf()
        withFormulaAndroid(
            configure = {
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
        ) {
            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.recreate()
            scenario.close()

            val lifecycle = listOf(CREATED, STARTED, RESUMED, STARTED, CREATED, DESTROYED)
            // We expect two full lifecycles
            val expected = listOf(INITIALIZED) + lifecycle + lifecycle
            assertThat(events).containsExactlyElementsIn(expected).inOrder()
        }
    }

    @Test
    fun `all updates except the last are dropped if they are emitted before Activity onStarted is called`() {
        runActivityUpdateTest(
            initialUpdates = Observable.just("one", "two", "three")
        ) { _, interactor ->
            // Only last update is received while others are dropped.
            val updates = interactor.currentUpdates()
            assertThat(updates).containsExactly("three")
        }
    }

    @Test
    fun `activity updates are emitted`() {
        runActivityUpdateTest { _, interactor ->
            interactor.publish("update-1")
            interactor.publish("update-2")

            val updates = interactor.currentUpdates()
            assertThat(updates).containsExactly("update-1", "update-2").inOrder()
        }
    }

    @Test
    fun `last activity update is emitted after configuration changes`() {
        runActivityUpdateTest { scenario, updateRelay ->
            updateRelay.publish("update-1")
            updateRelay.publish("update-2")
            scenario.recreate()

            val updates = updateRelay.currentUpdates()
            assertThat(updates).containsExactly("update-2").inOrder()
        }
    }

    @Test
    fun `activity updates observable is disposed when activity is finished`() {
        runActivityUpdateTest { scenario, updateRelay ->
            updateRelay.assertHasObservers(true)
            scenario.close()
            updateRelay.assertHasObservers(false)
        }
    }
}
