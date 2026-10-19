package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.internal.ActivityStoreContextImpl
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun doSomething() {}
    }

    private lateinit var context: ActivityStoreContextImpl<FakeActivity>

    @Before fun setup() {
        context = ActivityStoreContextImpl()
    }

    @Test fun `send drops events if activity is not started`() {
        val activity = createFakeActivity()
        context.attachActivity(activity)

        context.send {
            doSomething()
        }

        verifyNoInteractions(activity)
    }

    @Test fun `send event success`() {
        val activity = createFakeActivity()
        context.attachActivity(activity)
        context.onActivityStarted(activity)

        context.send {
            doSomething()
        }

        verify(activity).doSomething()
    }

    private fun createFakeActivity(): FakeActivity {
        return mock<FakeActivity>()
    }
}
