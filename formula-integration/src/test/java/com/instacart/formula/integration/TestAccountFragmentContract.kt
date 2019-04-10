package com.instacart.formula.integration

import android.view.View
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestAccountFragmentContract(
    override val tag: String = "account fragment",
    override val layoutId: Int = -1
) : FragmentContract<String>() {
    override fun createComponent(view: View): FragmentComponent<String> {
        return FragmentComponent.noOp()
    }
}
