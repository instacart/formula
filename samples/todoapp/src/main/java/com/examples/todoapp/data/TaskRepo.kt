package com.examples.todoapp.data

import com.examples.todoapp.tasks.TaskCompletedEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class TaskRepo {
    private val localStore: BehaviorRelay<List<Task>> = BehaviorRelay.createDefault(
        listOf(
            Task("Mow the lawn."),
            Task("Go get a haircut.")
        )
    )

    fun tasks(): Observable<List<Task>> {
        return localStore
    }

    fun onTaskCompleted(event: TaskCompletedEvent) {
        val updated = localStore.value!!.map {
            if (it.id == event.taskId) {
                it.copy(isCompleted = event.isCompleted)
            } else {
                it
            }
        }

        localStore.accept(updated)
    }
}
