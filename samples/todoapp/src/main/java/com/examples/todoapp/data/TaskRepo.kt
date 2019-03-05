package com.examples.todoapp.data

import io.reactivex.Observable

class TaskRepo {

    fun tasks(): Observable<List<Task>> {
        return Observable.just(listOf(
            Task("Mow the lawn."),
            Task("Go get a haircut.")
        ))
    }
}
