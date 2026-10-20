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

    @Test fun `initialized manager can be disposed`() {
        val factory = ActivityStoreFactory(
            activities = {
                activity(FakeActivity::class) {
                    ActivityStore()
                }
            }
        )

        val manager = factory.init(mock<FakeActivity>())
        assertThat(manager).isNotNull()
        manager?.dispose()
    }

    @Test fun `returns null if no binding for activity is found`() {
        val factory = ActivityStoreFactory(
            activities = {}
        )

        val store = factory.init(mock<FakeActivity>())
        assertThat(store).isNull()
    }
}
