package com.instacart.formula.internal

import com.instacart.formula.StreamConnection

class Frame(
    val workers: List<StreamConnection<*, *>>,
    val children: Map<FormulaKey, List<StreamConnection<*, *>>>
)
