package com.examples.todoapp.tasks

data class TaskFilterRenderModel(val title: String, val onSelected: () -> Unit)
