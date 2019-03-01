package com.instacart.formula

import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskDetailContract(
    val id: Int,
    override val tag: String = "task-$id",
    override val layoutId: Int = R.layout.basic_integration_task_list
) : FragmentContract<Any>() {
    override fun createComponent(view: View): FragmentComponent<Any> {
        return FragmentComponent.noOp()
    }
}
