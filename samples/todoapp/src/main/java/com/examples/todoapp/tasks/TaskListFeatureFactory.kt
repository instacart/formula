package com.examples.todoapp.tasks

import androidx.compose.runtime.Composable
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.android.ComposeViewFactory
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.runAsStateFlow

class TaskListFeatureFactory : FeatureFactory<TaskListFeatureFactory.Dependencies, TaskListKey>() {
    interface Dependencies {
        fun taskRepo(): TaskRepo
        fun taskListInput(): TaskListFormula.Input
    }

    override fun Params.initialize(): Feature {
        // Note: we could create our own internal dagger component here using the dependencies.
        return Feature(TaskListViewFactory()) {
            val formula = TaskListFormula(dependencies.taskRepo())
            formula.runAsStateFlow(it, dependencies.taskListInput())
        }
    }

    class TaskListViewFactory : ComposeViewFactory<TaskListRenderModel>() {
        @Composable
        override fun Content(model: TaskListRenderModel) {
            TaskListScreen(model)
        }
    }
}
