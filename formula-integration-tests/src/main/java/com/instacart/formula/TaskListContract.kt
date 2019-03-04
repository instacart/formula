package com.instacart.formula

import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskListContract(
    override val tag: String = "task list",
    override val layoutId: Int = R.layout.test_empty_layout
) : FragmentContract<String>() {
    override fun createComponent(view: View): FragmentComponent<String> {
        return TestFragmentComponent.create(this, view)
    }
}
