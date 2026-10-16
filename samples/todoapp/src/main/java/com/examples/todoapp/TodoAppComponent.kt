package com.examples.todoapp

import com.examples.todoapp.data.TaskRepo
import com.examples.todoapp.data.TaskRepoImpl
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.examples.todoapp.tasks.TaskListFormula

class TodoAppComponent(
    private val showToast: (String) -> Unit,
) : TaskListFeatureFactory.Dependencies {

    private val repo: TaskRepo = TaskRepoImpl()

    override fun taskRepo(): TaskRepo = repo

    override fun taskListInput(): TaskListFormula.Input {
        return TaskListFormula.Input(showToast = showToast)
    }
}
