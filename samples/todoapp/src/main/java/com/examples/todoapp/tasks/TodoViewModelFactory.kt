package com.examples.todoapp.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TodoViewModelFactory(
    private val creators: Map<Class<out ViewModel>, () -> ViewModel>,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")

        @Suppress("UNCHECKED_CAST")
        return creator() as T
    }
}
