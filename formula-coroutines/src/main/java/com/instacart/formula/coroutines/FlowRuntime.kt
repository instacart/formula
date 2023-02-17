package com.instacart.formula.coroutines

import com.instacart.formula.FormulaRuntime
import com.instacart.formula.IFormula
import com.instacart.formula.internal.ThreadChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object FlowRuntime {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <Input : Any, Output : Any> start(
        formula: IFormula<Input, Output>,
        input: Flow<Input>,
        mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
    ): Flow<Output> {
        val threadChecker = ThreadChecker(formula)
        return callbackFlow<Output> {
            threadChecker.check("Need to subscribe on main thread.")

            var runtime = FormulaRuntime(mainScope, threadChecker, formula, this::trySendBlocking, this::close)

            input.onEach { input ->
                threadChecker.check("Input arrived on a wrong thread.")
                if (!runtime.isKeyValid(input)) {
                    runtime.terminate()
                    runtime =
                        FormulaRuntime(mainScope, threadChecker, formula, this::trySendBlocking, this::close)
                }
                runtime.onInput(input)
            }.launchIn(this)

            awaitClose {
                threadChecker.check("Need to unsubscribe on the main thread.")
                runtime.terminate()
            }
        }.distinctUntilChanged()
    }
}