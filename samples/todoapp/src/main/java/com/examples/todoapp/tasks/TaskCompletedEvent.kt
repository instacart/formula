package com.examples.todoapp.tasks

data class TaskCompletedEvent(
    val taskId: String,
    val isCompleted: Boolean
)
