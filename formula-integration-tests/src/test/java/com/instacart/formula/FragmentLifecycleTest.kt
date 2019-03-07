package com.instacart.formula

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleTest {

    private lateinit var activityController: ActivityController<TestFragmentActivity>
    private lateinit var contract: TestLifecycleContract

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, TestFragmentActivity::class.java)
        val activityController = Robolectric.buildActivity(TestFragmentActivity::class.java, intent)
            .setup()
        val contract = activityController.get().contract
        this.activityController = activityController
        this.contract = contract
    }

    @Test fun `creation callbacks`() {
        assertThat(contract).isNotNull()
        assertThat(contract.hasOnViewCreated).isTrue()
        assertThat(contract.hasOnActivityCreated).isTrue()
        assertThat(contract.hasOnStart).isTrue()
        assertThat(contract.hasOnResume).isTrue()
    }

    @Test fun `destroy callbacks`() {
        activityController.destroy()
        assertThat(contract.hasOnPauseEvent).isTrue()
        assertThat(contract.hasOnStop).isTrue()
    }

    @Test fun `save instance state callback`() {
        activityController.saveInstanceState(Bundle())
        assertThat(contract.hasOnSaveInstanceState).isTrue()
    }

    // Unfortunately, we cannot test destroy view with Robolectric
    // https://github.com/robolectric/robolectric/issues/1945
}
