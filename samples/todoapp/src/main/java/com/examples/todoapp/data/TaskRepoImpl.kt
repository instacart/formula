package com.examples.todoapp.data

import com.examples.todoapp.tasks.TaskCompletedEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepoImpl : TaskRepo {
    private val localStore = MutableStateFlow(
        listOf(
            Task("Mow the lawn."),
            Task("Go get a haircut.")
        )
    )

    override fun tasks(): Flow<List<Task>> {
        // Fake initial network request
        return flow<Unit> { delay(5.seconds) }.flatMapLatest { localStore }
    }

    override fun onTaskCompleted(event: TaskCompletedEvent) {
        localStore.update { taskList ->
            taskList.map { task ->
                if (task.id == event.taskId) {
                    task.copy(isCompleted = event.isCompleted)
                } else {
                    task
                }
            }

        }
    }
}