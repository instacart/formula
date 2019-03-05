package com.examples.todoapp.tasks

class TaskItemRenderModel(
    val text: String,
    val isCompleted: Boolean,
    val onClick: () -> Unit,
    val onToggleCompleted: () -> Unit
)
