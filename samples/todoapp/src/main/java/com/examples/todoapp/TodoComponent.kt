package com.examples.todoapp

import com.examples.todoapp.data.TaskRepo
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.examples.todoapp.tasks.TaskListRenderModel
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.LifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class TodoComponent {
    private val repo: TaskRepo = TaskRepo()

    private val store: FragmentFlowStore = FragmentFlowStore.init {
        register(TaskListContract::class) {
            TaskListFormula(repo).state(TaskListFormula.Input())
        }
    }

    // Using a relay here to survive configutation changes.
    val stateRelay: BehaviorRelay<FragmentFlowState> = BehaviorRelay.create()

    init {
        store.state().subscribe(stateRelay::accept)
    }

    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    fun state(): Flowable<FragmentFlowState> {
        return stateRelay.toFlowable(BackpressureStrategy.LATEST)
    }
}
