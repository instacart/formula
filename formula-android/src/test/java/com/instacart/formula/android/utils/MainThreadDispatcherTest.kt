package com.instacart.formula.android.utils

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class MainThreadDispatcherTest {

    @Test
    fun `isDispatchNeeded returns false when on main thread`() {
        val result = MainThreadDispatcher().isDispatchNeeded()
        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `isDispatchNeeded returns true when not on main thread`() {
        val latch = CountDownLatch(1)
        val result = AtomicBoolean()
        Executors.newSingleThreadExecutor().execute {
            result.set(MainThreadDispatcher().isDispatchNeeded())
            latch.countDown()
        }

        if (latch.await(1, TimeUnit.SECONDS)) {
            Truth.assertThat(result.get()).isTrue()
        } else {
            error("Latch timed out!")
        }
    }

    @Test
    fun `if dispatch is called from main thread, executable is executed immediately`() {
        val dispatcher = MainThreadDispatcher()
        val loopers = mutableSetOf<Looper?>()
        dispatcher.dispatch { loopers.add(Looper.myLooper()) }
        Truth.assertThat(loopers).containsExactly(Looper.getMainLooper())
    }

    @Test
    fun `if dispatch is called from background thread, executable is dispatched to main thread`() {
        val dispatcher = MainThreadDispatcher()
        val latch = CountDownLatch(1)

        val loopers = mutableSetOf<Looper?>()
        Executors.newSingleThreadExecutor().execute {
            dispatcher.dispatch {
                loopers.add(Looper.myLooper())
            }
            latch.countDown()
        }

        if (latch.await(1, TimeUnit.SECONDS)) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Truth.assertThat(loopers).containsExactly(Looper.getMainLooper())
        } else {
            error("Latch timed out!")
        }
    }

}