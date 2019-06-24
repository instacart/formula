package com.instacart.formula

class Frame(
    val workers: List<Worker<*, *>>,
    val children: Map<ProcessorManager.FormulaKey, List<Worker<*, *>>>
)
