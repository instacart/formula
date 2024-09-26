package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.internal.ActivityStoreFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ActivityStoreFactoryTest {

    class FakeActivity : FragmentActivity()

    @Test fun `initialized store is live`() {
        val factory = ActivityStoreFactory(
            environment = FragmentEnvironment(),
            activities = {
                activity(FakeActivity::class) {
                    ActivityStore()
                }
            }
        )

        val store = factory.init(mock<FakeActivity>())!!
        assertThat(store.stateSubscription.isDisposed).isFalse()
    }

    @Test fun `returns null if no binding for activity is found`() {
        val factory = ActivityStoreFactory(
            environment = FragmentEnvironment(),
            activities = {}
        )

        val store = factory.init(mock<FakeActivity>())
        assertThat(store).isNull()
    }
}
