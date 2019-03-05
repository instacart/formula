package com.examples.todoapp.tasks

import android.view.View
import com.examples.todoapp.R
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskListContract(
    override val tag: String = "task list",
    override val layoutId: Int = R.layout.task_list
) : FragmentContract<TaskListRenderModel>() {

    override fun createComponent(view: View): FragmentComponent<TaskListRenderModel> {
        val renderView = TaskListRenderView(view)
        return FragmentComponent.create(renderView)
    }
}
