package com.instacart.formula.android.internal

import com.instacart.formula.android.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class EmptyFragmentKey(
    override val tag: String
) : FragmentKey
