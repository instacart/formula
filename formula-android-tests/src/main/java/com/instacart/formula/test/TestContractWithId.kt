package com.instacart.formula.test

import android.view.View
import com.instacart.formula.R
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestContractWithId(
    val id: Int,
    override val tag: String = "test-contract-$id",
    override val layoutId: Int = R.layout.test_empty_layout
) : FragmentContract<Any>() {
    override fun createComponent(view: View): FragmentComponent<Any> {
        return TestFragmentComponent.create(this, view)
    }
}
