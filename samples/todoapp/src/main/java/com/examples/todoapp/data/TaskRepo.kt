package com.examples.todoapp.data

import com.examples.todoapp.tasks.TaskCompletedEvent
import kotlinx.coroutines.flow.Flow

interface TaskRepo {

    fun tasks(): Flow<List<Task>>

    fun onTaskCompleted(event: TaskCompletedEvent)
}
