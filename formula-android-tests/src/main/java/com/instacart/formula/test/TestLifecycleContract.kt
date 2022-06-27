package com.instacart.formula.test

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestLifecycleContract(
    override val tag: String = "task list",
) : FragmentKey
