package com.instacart.testutils.android

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class StateFlowKey(
    val initAsync: Boolean,
    override val tag: String = "state flow key",
) : FragmentKey()
