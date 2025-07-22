package com.instacart.formula.internal

import com.instacart.formula.plugin.FormulaError

class FakeEffectDelegate : EffectDelegate {
    override val formulaType: Class<*> = Unit::class.java
    override val onError: (FormulaError) -> Unit = {}
}