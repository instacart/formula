package com.instacart.client.mvi

import android.view.View
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ICEmptyMviFragmentContract(
    override val tag: String
) : ICMviFragmentContract<Unit>() {

    @IgnoredOnParcel override val layoutId: Int = -1

    override fun createComponent(view: View): ICMviFragmentComponent<Unit> {
        return ICMviFragmentComponent.noOp()
    }
}