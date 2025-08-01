package com.instacart.formula.navigation

import android.os.Parcelable
import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class CounterFragmentKey(
    val fragmentId: Int,
    override val tag: String = "navigation-fragment-$fragmentId",
) : FragmentKey, Parcelable