package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.internal.ActivityStoreFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ActivityStoreFactoryTest {

    class FakeActivity : FragmentActivity()

    @Test fun `manager disposes navigation store and invokes onCleared`() {
        val navigationStore = mock<NavigationStore>()
        var wasCleared = false
        val factory = ActivityStoreFactory(
            activities = {
                activity(FakeActivity::class) {
                    ActivityStore(
                        navigationStore = navigationStore,
                        onCleared = { wasCleared = true },
                    )
                }
            }
        )

        val manager = factory.init(mock<FakeActivity>())!!
        manager.dispose()

        verify(navigationStore).dispose()
        assertThat(wasCleared).isTrue()
    }

    @Test fun `returns null if no binding for activity is found`() {
        val factory = ActivityStoreFactory(
            activities = {}
        )

        val store = factory.init(mock<FakeActivity>())
        assertThat(store).isNull()
    }
}
