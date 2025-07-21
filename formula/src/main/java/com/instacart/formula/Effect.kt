package com.instacart.formula

import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Plugin

/**
 * Effect is a function returned within [Transition.Result] which will be executed
 * by [FormulaRuntime]. The execution timing and thread will depend on the [Effect.Type]
 * specified with the default type being [Effect.Main]. This function is the place
 * to call listeners, log events, trigger network requests or database writes, and etc.
 * Instead of executing effects within [Transition.toResult], deferring execution
 * allows us to ensure that [Formula] is always in the correct state in case effects
 * trigger an update.
 */
data class Effect(
    val type: Type,
    private val formulaType: Class<*>,
    private val executable: () -> Unit,
) : () -> Unit {

    override fun invoke() {
        try {
            executable()
        } catch (throwable: Exception) {
            val error = FormulaError.EffectError(formulaType, throwable)
            FormulaPlugins.onError(error)
        }
    }

    /**
     * Defines the execution model of the effect such as timing and threading. Take a
     * look at [Unconfined], [Main], [Background].
     */
    sealed class Type

    /**
     * Unconfined effect type indicates that the effect can run on any thread and will be
     * executed after all state changes are processed. Given that any thread could run
     * this effect, the function should be thread-safe.
     */
    data object Unconfined : Type()

    /**
     * Immediate effect type indicates that the effect can run on any thread and can be executed
     * immediately before the state changes are processed and formula is re-evaluated. This
     * is a useful optimization for inlining multiple state changes as part of a single
     * re-evaluation.
     */
    // TODO: this will be added in a future.
//    data object Immediate: Type()

    /**
     * Indicates that the effect should be executed on the main thread and will be
     * executed after all state changes are processed.
     *
     * Note: Dispatcher should be set via [Plugin.mainThreadDispatcher]
     */
    data object Main : Type()

    /**
     * Indicates that the effect should be executed on the background thread and will be
     * executed after all state changes are processed.
     *
     * Note: Dispatcher should be set via [Plugin.backgroundThreadDispatcher]
     */
    data object Background : Type()
}
