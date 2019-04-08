package com.examples.todoapp

import com.examples.todoapp.data.TaskRepo
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.LifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

class TodoComponent {
    private val repo: TaskRepo = TaskRepo()

    private val activityEffectRelay: PublishRelay<TodoActivityEffect> = PublishRelay.create()

    private val store: FragmentFlowStore = FragmentFlowStore.init {
        register(TaskListContract::class) {
            TaskListFormula(repo).state(TaskListFormula.Input(showToast = { message ->
                activityEffectRelay.accept(TodoActivityEffect.ShowToast(message))
            }))
        }
    }

    // Using a relay here to survive configuration changes.
    val stateRelay: BehaviorRelay<FragmentFlowState> = BehaviorRelay.create()

    init {
        // This subscription should be added to some scope that outlives activity configuration changes.
        store.state().subscribe(stateRelay::accept)
    }

    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    fun state(): Flowable<FragmentFlowState> {
        return stateRelay.toFlowable(BackpressureStrategy.LATEST)
    }

    fun activityEffects(): Observable<TodoActivityEffect> {
        return activityEffectRelay
    }
}
