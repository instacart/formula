package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*

object FlowRuntime {
    fun <Input : Any, Output : Any> start(
        input: Flow<Input>,
        formula: IFormula<Input, Output>
    ): Flow<Output> {
        val threadChecker = ThreadChecker()
        return callbackFlow<Output> {
            threadChecker.check("Need to subscribe on main thread.")


            var runtime = FormulaRuntime(threadChecker, formula, ::trySendBlocking, channel::close)

            input.collect { input ->
                threadChecker.check("Input arrived on a wrong thread.")
                if (!runtime.isKeyValid(input)) {
                    runtime.terminate()
                    runtime =
                        FormulaRuntime(threadChecker, formula, ::trySendBlocking, channel::close)
                }
                runtime.onInput(input)
            }

            awaitClose {
                if(!channel.isClosedForSend) {
                    runtime.terminate()
                }
            }

        }.distinctUntilChanged()
    }
}