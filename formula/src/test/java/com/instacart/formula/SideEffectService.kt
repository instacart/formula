package com.instacart.formula

class SideEffectService : () -> Unit {
    var invoked = 0

    override fun invoke() {
        invoked += 1
    }
}
