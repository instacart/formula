package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class TaskListFormulaTest {

    @Test
    fun `change filter type`() {
        val repo = mock<TaskRepo>()
        whenever(repo.tasks()).thenReturn(Observable.just(
            listOf(
                Task("Mow the lawn."),
                Task("Go get a haircut.")
            )
        ))

        TaskListFormula(repo)
            .test(TaskListFormula.Input(showToast = {}))
            .renderModel {
                assertThat(items).hasSize(2)
            }
            .renderModel {
                filterOptions.first { it.title == "COMPLETED_TASKS" }.onSelected()
            }
            .renderModel {
                assertThat(items).isEmpty()
            }
    }

    @Test
    fun `change formula output`() {
        val formula = mock<TaskListFormula>()
        val filterOption = TaskFilterRenderModel(title = "test", onSelected = {})
        whenever(formula.start(any<TaskListFormula.Input>())).thenReturn(
            Observable.just(TaskListRenderModel(items = emptyList(), filterOptions = listOf(filterOption)))
        )
        formula.start(TaskListFormula.Input { })
            .test()
            .assertValueAt(0) {
                it.items.isEmpty() &&
                it.filterOptions.size == 1 &&
                it.filterOptions.first().title == "test"
            }
    }
}
