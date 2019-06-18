package com.instacart.formula.fragment

import android.view.View
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class EmptyFragmentContract(
    override val tag: String
) : FragmentContract<Unit>() {

    @IgnoredOnParcel override val layoutId: Int = -1

    override fun createComponent(view: View): FragmentComponent<Unit> {
        return FragmentComponent.noOp()
    }
}
