package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.FormulaContext
import com.instacart.formula.ProcessResult
import com.instacart.formula.ProcessorFormula
import com.instacart.formula.RxProcessor
import com.instacart.formula.Transition
import io.reactivex.disposables.Disposable

class TaskListFormula(
    private val repo: TaskRepo
) : ProcessorFormula<TaskListFormula.Input, TaskListState, Unit, TaskListRenderModel> {

    class Input(
        val showToast: (String) -> Unit
    )

    internal class TaskListProcessor(
        private val repo: TaskRepo
    ) : RxProcessor<Unit, List<Task>>() {
        override fun subscribe(input: Unit, onEvent: (List<Task>) -> Unit): Disposable {
            return repo.tasks().subscribe(onEvent)
        }
    }

    override fun initialState(input: Input): TaskListState {
        return TaskListState(taskState = emptyList(), filterType = TasksFilterType.ALL_TASKS)
    }

    override fun process(
        input: Input,
        state: TaskListState,
        context: FormulaContext<TaskListState, Unit>
    ): ProcessResult<TaskListRenderModel> {

        val items = createTaskList(
            state,
            onTaskCompletedEvent = repo::onTaskCompleted,
            showToast = input.showToast
        )

        return ProcessResult(
            workers = listOf(
                context.worker(TaskListProcessor(repo), Unit) {
                    Transition(state.copy(taskState = it))
                }
            ),
            renderModel = TaskListRenderModel(
                items = items,
                filterOptions = TasksFilterType.values().map { type ->
                    TaskFilterRenderModel(title = type.name, onSelected = {
                        context.transition(state.copy(filterType = type))
                    })
                })
        )
    }

    private fun createTaskList(
        state: TaskListState,
        onTaskCompletedEvent: (TaskCompletedEvent) -> Unit,
        showToast: (String) -> Unit
    ): List<TaskItemRenderModel> {
        val tasks = state.taskState.filter {
            when (state.filterType) {
                TasksFilterType.ALL_TASKS -> true
                TasksFilterType.COMPLETED_TASKS -> it.isCompleted
                TasksFilterType.ACTIVE_TASKS -> !it.isCompleted
            }
        }

        return tasks.map {
            TaskItemRenderModel(
                id = it.id,
                text = it.title,
                isSelected = it.isCompleted,
                onClick = {
                    showToast("Task selected: ${it.title}")
                },
                onToggle = {
                    onTaskCompletedEvent(
                        TaskCompletedEvent(
                            taskId = it.id,
                            isCompleted = !it.isCompleted
                        )
                    )
                }
            )
        }
    }
}
