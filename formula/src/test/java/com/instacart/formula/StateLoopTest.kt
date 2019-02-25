package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class StateLoopTest {
    sealed class Action() {
        object Increment : Action()
        object Decrement : Action()
    }

    object ClearState

    class Reducers : NextReducers<Int, ClearState>() {

        fun onAction(it: Action) = reduce { state ->
            val newState = when (it) {
                is Action.Increment -> state + 1
                is Action.Decrement -> state - 1
            }

            newState.toNextWithOptionalEffect(ClearState.takeIf { newState == 3 })
        }

        fun onClearState(action: ClearState) = withoutEffects {
            0
        }
    }

    /**
     * Make sure that event order is correct
     * when effects are pushed into a relay that modifies state.
     */
    @Test
    fun `state changes arrive in correct order`() {
        val actionRelay = BehaviorRelay.create<Action>()

        // We need to use behavior relay instead of publish relay because effects are emitted before
        // effect stream is subscribed to.
        val clearStateRelay = PublishRelay.create<ClearState>()

        val reducers = Reducers()

        val clearStateReducer = clearStateRelay
            .toFlowable(BackpressureStrategy.LATEST)
            .map(reducers::onClearState)

        val actionReducer = actionRelay
            .toFlowable(BackpressureStrategy.LATEST)
            .map(reducers::onAction)

        val testSubscriber = TestSubscriber<Int>()
        StateLoop(
            initialState = 0,
            reducers = Flowable.merge(actionReducer, clearStateReducer),
            onEffect = {
                clearStateRelay.accept(it)
            })
            .createLoop().subscribe(testSubscriber)

        // Increment 3 times to trigger clear state
        actionRelay.accept(Action.Increment)
        actionRelay.accept(Action.Increment)
        actionRelay.accept(Action.Increment)

        // We want to ensure that all updates come in order.
        val values = testSubscriber.values()
        assertThat(values[0]).isEqualTo(0)
        assertThat(values[1]).isEqualTo(1)
        assertThat(values[2]).isEqualTo(2)
        assertThat(values[3]).isEqualTo(3)
        assertThat(values[4]).isEqualTo(0)
    }

    @Test fun `notifies state updates`() {
        val modifyState = PublishRelay.create<Reducer<Int>>()
        val reducers = modifyState.toFlowable(BackpressureStrategy.BUFFER).map {
            { state: Int ->
                Next(it(state), emptySet<Unit>())
            }
        }

        val updates = mutableListOf<Int>()
        val loop = StateLoop<Int, Unit>(
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
