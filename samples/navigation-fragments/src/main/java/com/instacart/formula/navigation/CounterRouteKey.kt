package com.instacart.formula.navigation

import android.os.Parcelable
import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class CounterRouteKey(
    val fragmentId: Int,
    override val tag: String = "navigation-fragment-$fragmentId",
) : RouteKey, Parcelable