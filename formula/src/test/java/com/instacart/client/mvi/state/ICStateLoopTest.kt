package com.instacart.client.mvi.state

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import org.junit.Test

class ICStateLoopTest {
    @Test fun createLoop_notifiesStateChange() {
        val modifyState = PublishRelay.create<Reducer<Int>>()
        val reducers = modifyState.toFlowable(BackpressureStrategy.BUFFER).map {
            { state: Int ->
                ICNext(it(state), emptySet<Unit>())
            }
        }

        val updates = mutableListOf<Int>()
        val loop = ICStateLoop<Int, Unit>(
            initialState = 0,
            reducers = reducers,
            initialEffects = emptySet(),
            onStateChange = {
                updates.add(it)
            })

        loop.unsafeRun()
        modifyState.accept {
            it + 1
        }

        assertThat(updates).containsExactly(0, 1)
    }
}
