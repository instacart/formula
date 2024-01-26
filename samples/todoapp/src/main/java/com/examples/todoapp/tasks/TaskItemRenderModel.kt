package com.examples.todoapp.tasks

import com.instacart.formula.Listener

data class TaskItemRenderModel(
    val id: String,
    val text: String,
    val isSelected: Boolean,
    val onClick: Listener<Unit>,
    val onToggle: Listener<Unit>,
)
