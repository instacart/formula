package com.instacart.formula.internal

import com.instacart.formula.Update

/**
 * A frame is a representation of state after a process round. After each processing round
 * we need to look at what children and updates exist and do a diff.
 */
class Frame(
    val updates: List<Update>,
    val children: Map<FormulaKey, List<Update>>
)
