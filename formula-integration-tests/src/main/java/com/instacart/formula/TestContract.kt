package com.instacart.formula

import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestContract(
    override val tag: String = "test contract",
    override val layoutId: Int = R.layout.test_empty_layout
) : FragmentContract<Any>() {
    override fun createComponent(view: View): FragmentComponent<Any> {
        return TestFragmentComponent.create(this, view)
    }
}
