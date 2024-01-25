package com.instacart.formula.android.internal

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AndroidUpdateSchedulerTest {

    @Test fun `when an update triggers another update, scheduler finishes first one before proceeding to the next`() {
        val computedValues = LinkedList<String>()
        val scheduler = AndroidUpdateScheduler<() -> String> { valueComputation ->
            val value = valueComputation()
            computedValues.addLast(value)
        }

        scheduler.emitUpdate {
            scheduler.emitUpdate { "next" }
            "first"
        }

        assertThat(computedValues).containsExactly("first", "next").inOrder()
    }

    @Test fun `when update arrives on bg thread, handle it on main thread`() {
        val computedValues = LinkedList<String>()
        val scheduler = AndroidUpdateScheduler<() -> String> { valueComputation ->
            val value = valueComputation()
            computedValues.addLast(value)
        }

        val latch = CountDownLatch(1)
        Executors.newSingleThreadExecutor().execute {
            scheduler.emitUpdate { "bg update" }
            latch.countDown()
        }

        if (!latch.await(100, TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("timeout")
        }

        shadowOf(Looper.getMainLooper()).idle()
        assertThat(computedValues).containsExactly("bg update").inOrder()
    }

    @Test fun `when multiple updates arrive on bg thread before main thread is ready, we handle only last`() {
        val computedValues = LinkedList<String>()
        val scheduler = AndroidUpdateScheduler<() -> String> { valueComputation ->
            val value = valueComputation()
            computedValues.addLast(value)
        }

        val latch = CountDownLatch(1)
        Executors.newSingleThreadExecutor().execute {
            scheduler.emitUpdate { "bg update-1" }
            scheduler.emitUpdate { "bg update-2" }
            scheduler.emitUpdate { "bg update-3" }
            scheduler.emitUpdate { "bg update-4" }
            latch.countDown()
        }

        if (!latch.await(100, TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("timeout")
        }

        shadowOf(Looper.getMainLooper()).idle()
        assertThat(computedValues).containsExactly("bg update-4").inOrder()
    }
}