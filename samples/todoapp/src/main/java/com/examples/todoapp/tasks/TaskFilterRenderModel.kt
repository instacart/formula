package com.examples.todoapp.tasks

import com.instacart.formula.Listener

data class TaskFilterRenderModel(
    val title: String,
    val onSelected: Listener<Unit>,
)
