package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Test

class TaskListFormulaTest {

    @Test fun `change filter type`() {
        val repo = mock<TaskRepo>()
        whenever(repo.tasks()).thenReturn(Observable.just(
            listOf(
                Task("Mow the lawn."),
                Task("Go get a haircut.")
            )
        ))

        TaskListFormula(repo)
            .test(TaskListFormula.Input(showToast = {}))
            .apply {
                assertThat(values().last().items).hasSize(2)
            }
            .apply {
                values().last().filterOptions.first { it.title == "COMPLETED_TASKS" }.onSelected()
            }
            .apply {
                assertThat(values().last().items).isEmpty()
            }
    }
}
