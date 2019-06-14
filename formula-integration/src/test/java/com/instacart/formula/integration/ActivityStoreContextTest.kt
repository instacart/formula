package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun fakeEvents(): Observable<String> = Observable.empty()

        fun doSomething() {}
    }

    lateinit var holder: ActivityHolder<FakeActivity>
    lateinit var context: ActivityStoreContext<FakeActivity>
    val fakeEventRelay = PublishRelay.create<String>()

    @Before fun setup() {
        holder = ActivityHolder()
        context = ActivityStoreContext(holder)
    }

    @Test fun `select activity events only runs when activity is attached`() {
        val fakeEvents = context.selectActivityEvents { fakeEvents() }

        fakeEvents
            .test()
            .apply {
                fakeEventRelay.accept("missed")
            }
            .assertEmpty()
            .apply {
                val activity = createFakeActivity()
                holder.attachActivity(activity)

                fakeEventRelay.accept("first")
                fakeEventRelay.accept("second")

                holder.detachActivity(activity)

                fakeEventRelay.accept("third")
            }
            .assertValues("first", "second")
    }

    @Test fun `send drops events if activity is not started`() {
        val activity = createFakeActivity()
        holder.attachActivity(activity)

        context.send {
            doSomething()
        }

        verifyZeroInteractions(activity)
    }

    @Test fun `send event success`() {
        val activity = createFakeActivity()
        holder.attachActivity(activity)
        holder.onActivityStarted(activity)

        context.send {
            doSomething()
        }

        verify(activity).doSomething()
    }

    private fun createFakeActivity(): FakeActivity {
        val activity = mock<FakeActivity>()
        whenever(activity.fakeEvents()).thenReturn(fakeEventRelay)
        return activity
    }
}
