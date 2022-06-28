package com.instacart.formula.test

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestLifecycleKey(
    override val tag: String = "task list",
) : FragmentKey
