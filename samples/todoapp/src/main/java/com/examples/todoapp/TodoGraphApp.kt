package com.examples.todoapp

import com.examples.todoapp.data.TodoRepoImpl
import com.examples.todoapp.tasks.TodoViewModelFactory

class TodoGraphApp {

    private val todoRepo = TodoRepoImpl()

    fun viewModelFactory() = TodoViewModelFactory(todoRepo)
}
