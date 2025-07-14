package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.internal.ActivityStoreContextImpl
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import kotlinx.parcelize.Parcelize
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun fakeEvents(): Observable<String> = Observable.empty()

        fun doSomething() {}
    }

    private lateinit var context: ActivityStoreContextImpl<FakeActivity>
    val fakeEventRelay = PublishRelay.create<String>()

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

    @Test fun `is fragment started`() {
        val contract = createContract()
        context.isFragmentStarted(contract)
            .test()
            .apply {
                val instance = FragmentId("", contract)
                context.updateFragmentLifecycleState(instance, Lifecycle.State.STARTED)
            }
            .assertValues(false, true)
    }

    @Test fun `is fragment resumed`() {
        val contract = createContract()
        context.isFragmentResumed(contract)
            .test()
            .apply {
                val instance = FragmentId("", contract)
                context.updateFragmentLifecycleState(instance, Lifecycle.State.RESUMED)
            }
            .assertValues(false, true)
    }

    private fun createContract(): FragmentKey {
        return TestFragmentKey()
    }

    private fun createFakeActivity(): FakeActivity {
        val activity = mock<FakeActivity>()
        whenever(activity.fakeEvents()).thenReturn(fakeEventRelay)
        return activity
    }

    @Parcelize
    private data class TestFragmentKey(
        override val tag: String = "fake tag",
    ): FragmentKey()
}
