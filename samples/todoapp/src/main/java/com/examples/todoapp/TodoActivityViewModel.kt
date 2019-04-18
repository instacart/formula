package com.examples.todoapp

import androidx.lifecycle.ViewModel
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.LifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class TodoActivityViewModel : ViewModel() {
    // Should be injected
    private val component: TodoAppComponent = TodoAppComponent()

    private val activityEffectRelay: PublishRelay<TodoActivityEffect> = PublishRelay.create()

    private val store: FragmentFlowStore = FragmentFlowStore.init(component) {
        bind(TaskListContract::class) { component, key ->
            component.createTaskListFormula().state(TaskListFormula.Input(showToast = { message ->
                activityEffectRelay.accept(TodoActivityEffect.ShowToast(message))
            }))
        }
    }

    private val disposables = CompositeDisposable()

    // We use replay + connect so this stream survives configuration changes.
    val state: Flowable<FragmentFlowState> =  store.state().replay(1).apply {
        connect { disposables.add(it) }
    }

    // We expose effects on the activity.
    val effects: Observable<TodoActivityEffect> = activityEffectRelay

    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
