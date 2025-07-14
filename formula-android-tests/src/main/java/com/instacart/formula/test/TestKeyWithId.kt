package com.instacart.formula.test

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestKeyWithId(
    val id: Int,
    override val tag: String = "test-key-$id",
) : FragmentKey()
