package com.examples.todoapp.tasks

import androidx.lifecycle.ViewModel
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
}
