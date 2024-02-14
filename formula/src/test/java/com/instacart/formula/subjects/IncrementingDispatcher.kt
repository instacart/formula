package com.instacart.formula.subjects

import com.google.common.truth.Truth
import com.instacart.formula.plugin.Dispatcher
import java.util.concurrent.atomic.AtomicInteger

class IncrementingDispatcher : Dispatcher {
    val count = AtomicInteger(0)

    override fun dispatch(executable: () -> Unit) {
        count.incrementAndGet()
        executable()
    }

    fun assertCalled(times: Int) {
        Truth.assertThat(count.get()).isEqualTo(times)
    }
}