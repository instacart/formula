package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxStream

class TaskListFormula(
    private val repo: TaskRepo
) : Formula<TaskListFormula.Input, TaskListState, TaskListRenderModel>() {

    class Input(
        val showToast: (String) -> Unit
    )

    override fun initialState(input: Input): TaskListState {
        return TaskListState(taskState = emptyList(), filterType = TasksFilterType.ALL_TASKS)
    }

    override fun Snapshot<Input, TaskListState>.evaluate(): Evaluation<TaskListRenderModel> {
        val items = taskList(state).map { task ->
            context.key(task.id) {
                TaskItemRenderModel(
                    id = task.id,
                    text = task.title,
                    isSelected = task.isCompleted,
                    onClick = context.onEvent {
                        transition {
                            input.showToast("Task selected: ${task.title}")
                        }
                    },
                    onToggle = context.onEvent {
                        transition {
                            val event = TaskCompletedEvent(
                                taskId = task.id,
                                isCompleted = !task.isCompleted
                            )
                            repo.onTaskCompleted(event)
                        }
                    }
                )
            }
        }

        return Evaluation(
            updates = context.updates {
                RxStream.fromObservable(repo::tasks).onEvent {
                    transition(state.copy(taskState = it))
                }
            },
            output = TaskListRenderModel(
                items = items,
                filterOptions = filterOptions(state, context)
            )
        )
    }

    private fun taskList(state: TaskListState): List<Task> {
        return state.taskState.filter {
            when (state.filterType) {
                TasksFilterType.ALL_TASKS -> true
                TasksFilterType.COMPLETED_TASKS -> it.isCompleted
                TasksFilterType.ACTIVE_TASKS -> !it.isCompleted
            }
        }
    }

    private fun filterOptions(
        state: TaskListState,
        context: FormulaContext<TaskListState>
    ): List<TaskFilterRenderModel> {
        return TasksFilterType.values().map { type ->
            context.key(type.name) {
                TaskFilterRenderModel(
                    title = type.name,
                    onSelected = context.onEvent {
                        transition(state.copy(filterType = type))
                    }
                )
            }
        }
    }
}
