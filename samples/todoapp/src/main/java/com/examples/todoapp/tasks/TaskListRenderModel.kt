package com.examples.todoapp.tasks

data class TaskListRenderModel(
    val items: List<TaskItemRenderModel>,
    val filterOptions: List<TaskFilterRenderModel>
)
