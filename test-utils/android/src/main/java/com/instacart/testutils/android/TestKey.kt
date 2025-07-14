package com.instacart.testutils.android

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestKey(
    override val tag: String = "test key",
) : FragmentKey()
