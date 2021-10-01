package com.examples.todoapp.tasks

import com.instacart.formula.Listener

class TaskItemRenderModel(
    val id: String,
    val text: String,
    val isSelected: Boolean,
    val onClick: Listener<Unit>,
    val onToggle: Listener<Unit>,
)
