package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class ActivityProxyTest {
    class FakeActivity : FragmentActivity() {
        fun fakeEvents(): Observable<String> = Observable.empty()
    }

    lateinit var proxy: ActivityProxy<FakeActivity>
    val fakeEventRelay = PublishRelay.create<String>()

    @Before fun setup() {
        proxy = ActivityProxy()
    }

    @Test fun `select activity events only runs when activity is attached`() {
        val fakeEvents = proxy.selectActivityEvents { fakeEvents() }

        fakeEvents
            .test()
            .apply {
                fakeEventRelay.accept("missed")
            }
            .assertEmpty()
            .apply {
                val activity = createFakeActivity()
                proxy.attachActivity(activity)

                fakeEventRelay.accept("first")
                fakeEventRelay.accept("second")

                proxy.detachActivity(activity)

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
