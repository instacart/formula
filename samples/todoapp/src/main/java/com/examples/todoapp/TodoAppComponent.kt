package com.examples.todoapp

import com.examples.todoapp.data.TaskRepo
import com.examples.todoapp.data.TaskRepoImpl
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.android.ActivityStoreContext

class TodoAppComponent(
    private val store: ActivityStoreContext<TodoActivity>
) : TaskListFeatureFactory.Dependencies {

    private val repo: TaskRepo = TaskRepoImpl()

    override fun taskRepo(): TaskRepo {
        return repo
    }

    override fun taskListInput(): TaskListFormula.Input {
        return TaskListFormula.Input(showToast = { message ->
            store.send {
                onEffect(TodoActivityEffect.ShowToast(message))
            }
        })
    }
}