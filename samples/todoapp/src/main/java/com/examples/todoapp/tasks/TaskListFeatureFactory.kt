package com.examples.todoapp.tasks

import com.examples.todoapp.R
import com.examples.todoapp.data.TaskRepo
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.LayoutViewFactory
import com.instacart.formula.android.ViewInstance
import com.instacart.formula.rxjava3.toObservable

class TaskListFeatureFactory : FeatureFactory<TaskListFeatureFactory.Dependencies, TaskListKey> {
    interface Dependencies {
        fun taskRepo(): TaskRepo
        fun taskListInput(): TaskListFormula.Input
    }

    override fun initialize(dependencies: Dependencies, key: TaskListKey): Feature<*> {
        // Note: we could create our own internal dagger component here using the dependencies.
        val formula = TaskListFormula(dependencies.taskRepo())
        return Feature(
            state = formula.toObservable(dependencies.taskListInput()),
            viewFactory = TaskListViewFactory()
        )
    }

    class TaskListViewFactory : LayoutViewFactory<TaskListRenderModel>(R.layout.task_list) {
        override fun ViewInstance.create(): FeatureView<TaskListRenderModel> {
            return featureView(TaskListRenderView(view))
        }
    }
}