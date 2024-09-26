package com.instacart.formula.android.internal

import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.testutils.android.TestFragmentActivity
import com.instacart.testutils.android.activity
import com.instacart.testutils.android.executeOnBackgroundThread
import com.instacart.testutils.android.throwOnTimeout
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ActivityStoreContextTest {

    @Test fun `started activity returns null until onActivityStarted is called`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        val storeContext = ActivityStoreContextImpl<TestFragmentActivity>()

        // Initially null
        assertThat(storeContext.startedActivity()).isNull()

        // After attach
        storeContext.attachActivity(scenario.activity())
        assertThat(storeContext.startedActivity()).isNull()

        // After on started
        storeContext.onActivityStarted(scenario.activity())
        assertThat(storeContext.startedActivity()).isEqualTo(scenario.activity())
    }

    @Test fun `detaches only if the activity matches`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        val storeContext = ActivityStoreContextImpl<TestFragmentActivity>()

        val oldActivity = scenario.activity()
        val newActivity = scenario.recreate().activity()

        storeContext.attachActivity(newActivity)
        storeContext.onActivityStarted(newActivity)
        storeContext.detachActivity(oldActivity)

        assertThat(storeContext.startedActivity()).isEqualTo(newActivity)
    }

    @Test fun `send posts events on the main thread`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        val storeContext = ActivityStoreContextImpl<TestFragmentActivity>()
        storeContext.attachActivity(scenario.activity())
        storeContext.onActivityStarted(scenario.activity())

        val effectThread = mutableListOf<Looper?>()
        storeContext.send { effectThread.add(Looper.myLooper()) }
        storeContext.sendOnBackgroundThread { effectThread.add(Looper.myLooper()) }

        assertThat(effectThread).containsExactly(
            Looper.getMainLooper(), Looper.getMainLooper()
        )
    }

    @Test
    fun `send drops the action if there is no started activity`() {
        val storeContext = ActivityStoreContextImpl<TestFragmentActivity>()

        val effectThread = mutableListOf<Looper?>()
        storeContext.send { effectThread.add(Looper.myLooper()) }

        val result = runCatching {
            storeContext.sendOnBackgroundThread { effectThread.add(Looper.myLooper()) }
        }
        assertThat(effectThread).isEmpty()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains(
            "timeout"
        )
    }

    private fun <ActivityType : FragmentActivity> ActivityStoreContextImpl<ActivityType>.sendOnBackgroundThread(
        action: ActivityType.() -> Unit
    ) {
        val sendLatch = CountDownLatch(1)
        executeOnBackgroundThread {
            send {
                action()
                sendLatch.countDown()
            }
        }
        sendLatch.throwOnTimeout()
    }
}