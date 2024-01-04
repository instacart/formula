package com.examples.todoapp.data

import com.examples.todoapp.tasks.TaskCompletedEvent
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class TaskRepoImpl : TaskRepo {
    private val localStore: BehaviorRelay<List<Task>> = BehaviorRelay.createDefault(
        listOf(
            Task("Mow the lawn."),
            Task("Go get a haircut.")
        )
    )

    override fun tasks(): Observable<List<Task>> {
        // Fake initial network request
        return Observable.timer(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).flatMap {
            localStore
        }
    }

    override fun onTaskCompleted(event: TaskCompletedEvent) {
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