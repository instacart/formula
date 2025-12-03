package com.instacart.formula.android.internal

import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class EmptyRouteKey(
    override val tag: String
) : RouteKey
