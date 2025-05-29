package com.instacart.formula.rxjava3

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.jakewharton.rxrelay3.PublishRelay
import org.junit.Test

class RxJavaRuntimeTest {
    @Test fun `toFlow without an input`() {
        val formula = object : StatelessFormula<Unit, String>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = "value",
                )
            }
        }

        val observer = formula.toObservable().test()
        observer.assertValues("value")
        observer.assertNotComplete()
    }

    @Test fun `toFlow with a single input`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = input,
                )
            }
        }

        val observer = formula.toObservable("input").test()
        observer.assertValues("input")
        observer.assertNotComplete()
    }

    @Test fun `toFlow with input observable`() {
        val relay = PublishRelay.create<String>()
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(
                    output = input,
                )
            }
        }
        val observer = formula.toObservable(relay).test()
        observer.assertValues()

        relay.accept("input-1")
        observer.assertValues("input-1")

        relay.accept("input-2")
        observer.assertValues("input-1", "input-2")

        observer.assertNotComplete()
    }
}