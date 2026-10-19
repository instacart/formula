package com.examples.todoapp.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.examples.todoapp.TodoActivityEffect
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.runAsStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class TodoViewModel(
    taskRepo: TaskRepo,
) : ViewModel() {

    private val effectChannel = Channel<TodoActivityEffect>(Channel.BUFFERED)

    val effects = effectChannel.receiveAsFlow()

    val outputs = TaskListFormula(taskRepo).runAsStateFlow(
        scope = viewModelScope,
        input = TaskListFormula.Input { message ->
            effectChannel.trySend(TodoActivityEffect.ShowToast(message))
        },
    )

    class Factory(private val taskRepo: TaskRepo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(taskRepo) as T
        }
    }
}
