package com.instacart.formula.android.internal

import com.instacart.formula.fragment.FragmentKey
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class EmptyFragmentKey(
    override val tag: String
) : FragmentKey
