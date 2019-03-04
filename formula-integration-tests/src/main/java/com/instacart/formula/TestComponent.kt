package com.instacart.formula

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.LifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class TestComponent {
    val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

    private val stateChangeRelay = PublishRelay.create<Pair<FragmentContract<*>, Any>>()

    private val store = FragmentFlowStore.init {
        register(TaskListContract::class) {
            stateChanges(it)
        }

        register(TaskDetailContract::class) {
            stateChanges(it)
        }
    }

    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    // Share state
    val state = store.state().replay(1).refCount()

    fun <T : Any> sendStateUpdate(contract: FragmentContract<T>, update: T) {
        stateChangeRelay.accept(Pair<FragmentContract<*>, Any>(contract, update))
    }

    fun currentFragmentState(): FragmentFlowState {
        return state.test().values().last()
    }

    private fun stateChanges(contract: FragmentContract<*>): Flowable<Any> {
        return stateChangeRelay
            .toFlowable(BackpressureStrategy.BUFFER)
            .filter { event ->
                event.first == contract
            }
            .map { it.second }
    }
}
