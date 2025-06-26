package com.instacart.formula.android

import android.app.Application
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.test.runFeatureFactoryLifecycleTest
import com.instacart.testutils.android.TestActivity
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.TestFragmentActivity
import com.instacart.testutils.android.activity
import com.instacart.testutils.android.withFormulaAndroid
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
            assertThat(error).isEqualTo("can only initialize the store once.")
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
        assertThat(errorMessage).isEqualTo(
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
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> {
                    ActivityStore()
                }
            }
        ) { interactor ->
            val activityLifecycleEvents = interactor
                .selectEvents { it.activityLifecycleState() }
                .test()

            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.recreate()
            scenario.close()

            val lifecycle = listOf(CREATED, STARTED, RESUMED, STARTED, CREATED, DESTROYED)
            // We expect two full lifecycles
            val expected = listOf(INITIALIZED) + lifecycle + lifecycle
            assertThat(activityLifecycleEvents.values()).containsExactlyElementsIn(expected).inOrder()
        }
    }

    @Test
    fun `activity results are emitted`() {
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> { ActivityStore() }
            }
        ) { interactor ->
            val activityResultEvents = interactor
                .selectEvents { it.activityResults() }
                .test()

            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            FormulaAndroid.onActivityResult(scenario.activity(), 1, 2, null)

            activityResultEvents.assertValues(
                ActivityResult(1, 2, null)
            )
        }
    }

    @Test
    fun `feature factory lifecycle events`() {
        runFeatureFactoryLifecycleTest {
            assertThat(lifecycleCallback.hasOnViewCreated).isTrue()
            assertThat(lifecycleCallback.hasOnActivityCreated).isTrue()
            assertThat(lifecycleCallback.hasOnStart).isTrue()
            assertThat(lifecycleCallback.hasOnResume).isTrue()

            scenario.close()

            assertThat(lifecycleCallback.hasOnPauseEvent).isTrue()
            assertThat(lifecycleCallback.hasOnStop).isTrue()
            assertThat(lifecycleCallback.hasOnDestroyView).isTrue()
        }
    }

    @Test
    fun `feature factory save instance event`() {
        runFeatureFactoryLifecycleTest {
            assertThat(lifecycleCallback.hasOnSaveInstanceState).isFalse()
            scenario.recreate()
            assertThat(lifecycleCallback.hasOnSaveInstanceState).isTrue()
        }
    }

    @Test
    fun `feature factory low memory event`() {
        runFeatureFactoryLifecycleTest {
            scenario.onActivity {
                val fragment = it.supportFragmentManager.fragments
                    .filterIsInstance<FormulaFragment>()
                    .first()

                fragment.onLowMemory()
            }
            assertThat(lifecycleCallback.hasCalledLowMemory).isTrue()
        }
    }
}
