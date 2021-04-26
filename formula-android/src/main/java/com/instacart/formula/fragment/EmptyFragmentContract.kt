package com.instacart.formula.fragment

import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmptyFragmentContract(
    override val tag: String
) : FragmentKey
