package com.instacart.formula.android.fakes

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestSignUpFragmentKey(
    override val tag: String = "sign up fragment",
) : FragmentKey
