package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.start
import com.instacart.formula.test.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class TaskListFormulaTest {

    @Test fun `change filter type`() {
        val repo = mockk<TaskRepo>()
        every { repo.tasks() } returns Observable.just(
            listOf(
            Task("Mow the lawn."),
            Task("Go get a haircut.")
            )
        )

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

    @Test fun `change formula output`() {
        val formula = mockk<TaskListFormula>()
        val filterOption = TaskFilterRenderModel(title = "test", onSelected = {})
        mockkStatic("com.instacart.formula.RuntimeExtensionsKt")
        every { formula.start(any<TaskListFormula.Input>()) } returns Observable.just(
            TaskListRenderModel(items = emptyList(), filterOptions = listOf(filterOption))
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
