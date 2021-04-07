package com.examples.todoapp

import com.examples.todoapp.data.TaskRepo
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.integration.ActivityStoreContext

class TodoAppComponent(
    private val store: ActivityStoreContext<TodoActivity>
) : TaskListFeatureFactory.Dependencies {
    private val repo: TaskRepo = TaskRepo()

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