package com.instacart.formula

import androidx.lifecycle.ViewModel
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

class TestFragmentFlowViewModel : ViewModel() {
    val renderCalls = mutableListOf<Pair<FragmentContract<*>, *>>()

    private val stateChangeRelay = PublishRelay.create<Pair<FragmentContract<*>, Any>>()

    private val store = FragmentFlowStore.init {
        register(TaskListContract::class) {
            stateChanges(it)
        }

        register(TaskDetailContract::class) {
            stateChanges(it)
        }

        register(TestLifecycleContract::class) {
            stateChanges(it)
        }
    }

    private val disposables = CompositeDisposable()

    fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        store.onLifecycleEffect(event)
    }

    // Share state
    val state: Flowable<FragmentFlowState> = store.state().replay(1).apply {
        connect { disposables.add(it) }
    }

    fun <T : Any> sendStateUpdate(contract: FragmentContract<T>, update: T) {
        stateChangeRelay.accept(Pair<FragmentContract<*>, Any>(contract, update))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
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
