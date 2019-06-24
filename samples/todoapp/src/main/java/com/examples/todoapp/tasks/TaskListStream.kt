package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.Stream
import io.reactivex.disposables.Disposable

class TaskListStream(private val repo: TaskRepo) : Stream<Unit, List<Task>> {

    override fun subscribe(input: Unit, onEvent: (List<Task>) -> Unit): Disposable {
        return repo.tasks().subscribe(onEvent)
    }
}
