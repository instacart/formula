package com.instacart.formula.android.fakes

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainKey(
    val id: Int,
    override val tag: String = "main-$id",
) : FragmentKey
