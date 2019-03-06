package com.examples.todoapp.data

import java.util.UUID

data class Task(
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val id: String = UUID.randomUUID().toString()
)
