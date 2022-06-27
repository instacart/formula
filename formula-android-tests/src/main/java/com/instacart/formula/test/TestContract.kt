package com.instacart.formula.test

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestContract(
    override val tag: String = "test contract",
) : FragmentKey
