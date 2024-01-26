package com.examples.todoapp.data

import com.examples.todoapp.tasks.TaskCompletedEvent
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import java.util.concurrent.TimeUnit

interface TaskRepo {

    fun tasks(): Observable<List<Task>>

    fun onTaskCompleted(event: TaskCompletedEvent)
}
