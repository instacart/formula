package com.examples.todoapp.data

data class Task(
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false
)
