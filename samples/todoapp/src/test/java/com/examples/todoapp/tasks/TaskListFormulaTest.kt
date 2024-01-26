package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class TaskListFormulaTest {

    private val showToast = { toast: String ->

    }

    @Test
    fun `change filter type`() {
        val repo = TaskRepoFake()

        TaskListFormula(repo)
            .test(TaskListFormula.Input(showToast = showToast))
            .output {
                assertThat(items).hasSize(2)
            }
            .output {
                filterOptions.first { it.title == "COMPLETED_TASKS" }.onSelected(Unit)
            }
            .output {
                assertThat(items).isEmpty()
            }
    }

    class TaskRepoFake : TaskRepo {
        override fun tasks(): Observable<List<Task>> {
            return Observable.just(
                listOf(
                    Task("Mow the lawn."),
                    Task("Go get a haircut.")
                )
            )
        }

        override fun onTaskCompleted(event: TaskCompletedEvent) {
            // nothing to do
        }
    }
}
