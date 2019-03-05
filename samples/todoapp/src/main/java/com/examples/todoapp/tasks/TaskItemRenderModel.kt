package com.examples.todoapp.tasks

class TaskItemRenderModel(
    val id: String,
    val text: String,
    val isSelected: Boolean,
    val onClick: () -> Unit,
    val onToggle: () -> Unit
)
