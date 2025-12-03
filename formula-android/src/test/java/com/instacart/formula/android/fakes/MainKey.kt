package com.instacart.formula.android.fakes

import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainKey(
    val id: Int,
    override val tag: String = "main-$id",
) : RouteKey
