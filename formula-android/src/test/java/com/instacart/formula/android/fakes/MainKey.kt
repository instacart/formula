package com.instacart.formula.android.fakes

import android.view.View
import com.instacart.formula.android.FragmentComponent
import com.instacart.formula.android.FragmentContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainKey(
    val id: Int,
    override val tag: String = "main-$id",
    override val layoutId: Int = -1
) : FragmentContract<String>() {
    override fun createComponent(view: View): FragmentComponent<String> {
        return FragmentComponent.noOp()
    }
}
