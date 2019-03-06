package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task

data class TaskListState(
    val taskState: List<Task>,
    val filterType: TasksFilterType
)
