package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.toFlow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class CoroutineTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    override val formula: FormulaT,
    private val runtimeConfig: RuntimeConfig,
): FormulaTestDelegate<Input, Output, FormulaT> {
    private val values = mutableListOf<Output>()
    private val errors = mutableListOf<Throwable>()

    private val inputFlow = MutableSharedFlow<Input>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val job = GlobalScope.launch(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
    ) {
        formula.toFlow(inputFlow, runtimeConfig)
            .catch { errors.add(it) }
            .collect { values.add(it) }
    }

    override fun values(): List<Output> {
        return values
    }

    override fun input(input: Input) {
        inputFlow.tryEmit(input)
    }

    override fun assertNoErrors() {
        val error = errors.lastOrNull()
        if (error != null) {
            throw error
        }
    }

    override fun dispose() {
        job.cancel()
    }
}