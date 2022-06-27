package com.instacart.formula.android.fakes

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestLoginFragmentKey(
    override val tag: String = "login fragment",
) : FragmentKey
