package com.instacart.formula.test

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestContractWithId(
    val id: Int,
    override val tag: String = "test-contract-$id",
) : FragmentKey
