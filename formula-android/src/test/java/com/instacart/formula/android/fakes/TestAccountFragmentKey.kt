package com.instacart.formula.android.fakes

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestAccountFragmentKey(
    override val tag: String = "account fragment",
) : FragmentKey
