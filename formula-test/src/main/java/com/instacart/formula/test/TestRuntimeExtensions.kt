package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaObserver.input].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    parentScope: CoroutineScope,
    isValidationEnabled: Boolean = true,
    failOnError: Boolean = true,
    inspector: Inspector? = null,
    dispatcher: Dispatcher? = null,
): TestFormulaObserver<Input, Output, F> {
    return test(
        parentContext = parentScope.coroutineContext,
        isValidationEnabled = isValidationEnabled,
        failOnError = failOnError,
        inspector = inspector,
        dispatcher = dispatcher,
    )
}

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaObserver.input].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    parentContext: CoroutineContext,
    isValidationEnabled: Boolean = true,
    failOnError: Boolean = true,
    inspector: Inspector? = null,
    dispatcher: Dispatcher? = null,
): TestFormulaObserver<Input, Output, F> {
    return TestFormulaObserver(
        parentContext = parentContext,
        isValidationEnabled = isValidationEnabled,
        failOnError = failOnError,
        inspector = inspector,
        dispatcher = dispatcher,
        formula = this,
    )
}

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaObserver.input].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.testUnscoped(
    isValidationEnabled: Boolean = true,
    failOnError: Boolean = true,
    inspector: Inspector? = null,
    dispatcher: Dispatcher? = null,
): TestFormulaObserver<Input, Output, F> {
    return test(
        parentContext = EmptyCoroutineContext,
        isValidationEnabled = isValidationEnabled,
        failOnError = failOnError,
        inspector = inspector,
        dispatcher = dispatcher,
    )
}

suspend fun <Event> Action<Event>.test(
    callback: suspend TestActionObserver<Event>.() -> Unit,
) {
    val scope = newScope()
    try {
        val observer = TestActionObserver(this@test, scope)
        callback(observer)
    } finally {
        scope.cancel()
    }
}

private suspend fun newScope(): CoroutineScope {
    val dispatcher = getDispatcher()
    return CoroutineScope(coroutineContext + SupervisorJob() + dispatcher)
}

@OptIn(ExperimentalCoroutinesApi::class) // UnconfinedTestDispatcher is still experimental.
private suspend fun getDispatcher(): CoroutineDispatcher {
    // Use test-specific unconfined if test scheduler is in use to inherit its virtual time.
    return coroutineContext[TestCoroutineScheduler]?.let(::UnconfinedTestDispatcher) ?: Dispatchers.Unconfined
}

