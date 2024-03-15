package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

class InputIdentityFormula<Input : Any> : StatelessFormula<Input, Input>() {
    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Input> {
        return Evaluation(
            output = input,
        )
    }
}
