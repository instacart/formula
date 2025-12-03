package com.examples.todoapp.tasks

import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskListKey(
    override val tag: String = "task list",
) : RouteKey
