package com.examples.todoapp

import com.examples.todoapp.data.TaskRepoImpl
import com.examples.todoapp.tasks.TodoViewModelFactory

class TodoGraphApp {

    private val taskRepo = TaskRepoImpl()

    fun viewModelFactory() = TodoViewModelFactory(taskRepo)
}
