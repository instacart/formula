package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.internal.ActivityStoreFactory
import org.junit.Test
import org.mockito.kotlin.mock

class ActivityStoreFactoryTest {

    class FakeActivity : FragmentActivity()

    @Test fun `initialized store is live`() {
        val factory = ActivityStoreFactory(
            environment = FragmentEnvironment(),
            activities = {
                activity(FakeActivity::class) {
                    store { }
                }
            }
        )

        val store = factory.init(mock<FakeActivity>())!!
        assertThat(store.stateSubscription.isDisposed).isFalse()
    }
}
