package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TaskListFormulaTest {

    private val showToast = { toast: String ->

    }

    @Test
    fun `change filter type`() = runTest {
        val repo = TaskRepoFake()

        TaskListFormula(repo)
            .test(coroutineContext)
            .input(TaskListFormula.Input(showToast = showToast))
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

        override fun tasks(): Flow<List<Task>> {
            return flow {
                val tasks = listOf(
                    Task("Mow the lawn."),
                    Task("Go get a haircut.")
                )
                emit(tasks)
            }
        }

        override fun onTaskCompleted(event: TaskCompletedEvent) {
            // nothing to do
        }
    }
}
