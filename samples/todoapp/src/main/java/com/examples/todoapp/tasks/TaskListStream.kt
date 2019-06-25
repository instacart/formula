package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.RxStream
import io.reactivex.Observable

class TaskListStream(private val repo: TaskRepo) : RxStream<Unit, List<Task>> {

    override fun observable(input: Unit): Observable<List<Task>> {
        return repo.tasks()
    }
}
