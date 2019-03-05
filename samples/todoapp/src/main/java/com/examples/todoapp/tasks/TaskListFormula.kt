package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.Reducers
import com.instacart.formula.RenderFormula
import com.instacart.formula.RenderLoop
import com.instacart.formula.RenderModelGenerator
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

class TaskListFormula(
    private val repo: TaskRepo
) : RenderFormula<TaskListFormula.Input, TaskListState, Unit, TaskListRenderModel> {

    class Input

    override fun createRenderLoop(input: Input): RenderLoop<TaskListState, Unit, TaskListRenderModel> {
        val modifications = Modifications()

        val filterTypeRelay: PublishRelay<TasksFilterType> = PublishRelay.create()

        val changes = Observable.merge(
            listOf(
                repo.tasks().map(modifications::onTaskListChanged),
                filterTypeRelay.map(modifications::onFilterTypeChanged)
            )
        )

        return RenderLoop(
            initialState = TaskListState(taskState = emptyList(), filterType = TasksFilterType.ALL_TASKS),
            reducers = changes.toFlowable(BackpressureStrategy.LATEST),
            renderModelGenerator = RenderModelGenerator.create {
                val items = createTaskList(it, onTaskCompletedEvent = repo::onTaskCompleted)
                TaskListRenderModel(
                    items = items,
                    filterOptions = TasksFilterType.values().map { type ->
                        TaskFilterRenderModel(title = type.name, onSelected = {
                            filterTypeRelay.accept(type)
                        })
                    })
            }
        )
    }

    private fun createTaskList(
        state: TaskListState,
        onTaskCompletedEvent: (TaskCompletedEvent) -> Unit
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
                    // TODO show task detail page.
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

    class Modifications : Reducers<TaskListState, Unit>() {
        fun onTaskListChanged(newList: List<Task>) = withoutEffects {
            it.copy(taskState = newList)
        }

        fun onFilterTypeChanged(filterType: TasksFilterType) = withoutEffects {
            it.copy(filterType = filterType)
        }
    }
}
