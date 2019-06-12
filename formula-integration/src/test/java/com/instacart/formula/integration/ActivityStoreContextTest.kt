package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class ActivityStoreContextTest {
    class FakeActivity : FragmentActivity() {
        fun fakeEvents(): Observable<String> = Observable.empty()
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

    private fun createFakeActivity(): FakeActivity {
        val activity = mock<FakeActivity>()
        whenever(activity.fakeEvents()).thenReturn(fakeEventRelay)
        return activity
    }
}
