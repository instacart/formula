package com.examples.todoapp.tasks

import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula

class TaskListFormula(
    private val repo: TaskRepo
) : Formula<TaskListFormula.Input, TaskListState, TaskListRenderModel> {

    class Input(
        val showToast: (String) -> Unit
    )

    override fun initialState(input: Input): TaskListState {
        return TaskListState(taskState = emptyList(), filterType = TasksFilterType.ALL_TASKS)
    }

    override fun evaluate(
        input: Input,
        state: TaskListState,
        context: FormulaContext<TaskListState>
    ): Evaluation<TaskListRenderModel> {
        val items = createTaskList(
            state,
            onTaskCompletedEvent = repo::onTaskCompleted,
            showToast = input.showToast
        )

        return Evaluation(
            updates = context.updates {
                events("task changes", repo.tasks()) {
                    state.copy(taskState = it).noMessages()
                }
            },
            renderModel = TaskListRenderModel(
                items = items,
                filterOptions = TasksFilterType.values().map { type ->
                    context.key(type.name) {
                        TaskFilterRenderModel(title = type.name, onSelected = context.callback {
                            state.copy(filterType = type).noMessages()
                        })
                    }
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
