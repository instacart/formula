package com.instacart.formula.android.fakes

import android.view.View
import com.instacart.formula.android.FragmentComponent
import com.instacart.formula.android.FragmentContract
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestTaskListContract(
    override val tag: String = "task list fragment",
    override val layoutId: Int = -1
) : FragmentContract<String>() {
    override fun createComponent(view: View): FragmentComponent<String> {
        return FragmentComponent.noOp()
    }
}
