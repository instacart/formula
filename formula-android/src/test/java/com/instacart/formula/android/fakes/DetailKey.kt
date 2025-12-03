package com.instacart.formula.android.fakes

import com.instacart.formula.android.RouteKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailKey(
    val id: Int,
    override val tag: String = "detail-$id",
) : RouteKey