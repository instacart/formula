package com.examples.todoapp.tasks

import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.FormulaContext
import com.instacart.formula.ProcessResult
import com.instacart.formula.ProcessorFormula
import com.instacart.formula.Transition

class TaskListFormula(
    private val repo: TaskRepo
) : ProcessorFormula<TaskListFormula.Input, TaskListState, Unit, TaskListRenderModel> {

    class Input(
        val showToast: (String) -> Unit
    )

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
            streams = context.streams {
                events("task changes", repo.tasks()) {
                    Transition(state.copy(taskState = it))
                }
            },
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
