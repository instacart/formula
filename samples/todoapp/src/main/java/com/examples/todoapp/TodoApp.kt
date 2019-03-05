package com.examples.todoapp

import android.app.Application
import android.content.Context
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Flowable

class TodoApp : Application() {
    companion object {
        fun component(context: Context): TodoComponent {
            return (context.applicationContext as TodoApp).component
        }
    }

    private val component: TodoComponent = TodoComponent()
}
