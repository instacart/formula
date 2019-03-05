package com.examples.todoapp.tasks

import com.examples.todoapp.data.Task
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.Reducers
import com.instacart.formula.RenderFormula
import com.instacart.formula.RenderLoop
import com.instacart.formula.RenderModelGenerator
import com.jakewharton.rxrelay2.BehaviorRelay
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
                val items = it.taskState.map {
                    TaskItemRenderModel(
                        text = it.title,
                        isCompleted = it.isCompleted,
                        onClick = {
                            // TODO
                        },
                        onToggleCompleted = {
                            // TODO
                        }
                    )
                }
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

    class Modifications : Reducers<TaskListState, Unit>() {

        fun onTaskListChanged(newList: List<Task>) = withoutEffects {
            it.copy(taskState = newList)
        }

        fun onFilterTypeChanged(filterType: TasksFilterType) = withoutEffects {
            it.copy(filterType = filterType)
        }
    }
}
