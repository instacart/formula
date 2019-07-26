package com.instacart.formula.internal

import com.instacart.formula.Child
import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State, Output> internal constructor(
    private val processingPass: Long,
    private val delegate: Delegate<State, Output>,
    private val transitionCallback: TransitionCallbackWrapper<State, Output>
) : FormulaContext<State, Output>() {

    private var childBuilder: Child<State, Output, *, *, *> = Child<State, Output, Any, Any, Any>(this)

    internal var callbackCount = 0

    val children = mutableMapOf<FormulaKey, List<Update>>()
    val callbacks = mutableSetOf<String>()
    val eventCallbacks = mutableSetOf<String>()

    interface Delegate<State, Effect> {
        fun initOrFindCallback(key: String): Callback

        fun <UIEvent> initOrFindEventCallback(key: String): EventCallback<UIEvent>

        fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>,
            processingPass: Long
        ): Evaluation<ChildRenderModel>
    }

    private fun wrapCallback(wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        ensureNotRunning()
        return {
            transitionCallback(wrap(Transition.Factory))
        }
    }

    private fun <UIEvent> wrapEventCallback(wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit {
        ensureNotRunning()
        return {
            transitionCallback(wrap(Transition.Factory, it))
        }
    }

    override fun callback(wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        ensureNotRunning()

        val callback = callback(key = generatedKey(), wrap = wrap)
        incrementCallbackCount()
        return callback
    }

    override fun optionalCallback(
        condition: Boolean,
        wrap: Transition.Factory.() -> Transition<State, Output>
    ): (() -> Unit)? {
        return if (condition) {
            callback(wrap)
        } else {
            incrementCallbackCount()
            null
        }
    }

    override fun <UIEvent> eventCallback(wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit {
        val callback = eventCallback(key = generatedKey(), wrap = wrap)
        incrementCallbackCount()
        return callback
    }

    override fun <UIEvent> optionalEventCallback(
        condition: Boolean,
        wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): ((UIEvent) -> Unit)? {
        return if (condition) {
            eventCallback(wrap)
        } else {
            incrementCallbackCount()
            null
        }
    }

    override fun callback(key: String, wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        if (callbacks.contains(key)) {
            throw IllegalStateException("Callback $key is already defined. Make sure your key is unique.")
        }

        callbacks.add(key)

        val callback = delegate.initOrFindCallback(key)
        callback.callback = wrapCallback(wrap)
        return callback
    }

    override fun <UIEvent> eventCallback(
        key: String,
        wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): (UIEvent) -> Unit {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        if (eventCallbacks.contains(key)) {
            throw IllegalStateException("Event callback $key is already defined. Make sure your key is unique.")
        }

        eventCallbacks.add(key)

        val callback = delegate.initOrFindEventCallback<UIEvent>(key)
        callback.callback = wrapEventCallback(wrap)
        return callback
    }

    override fun updates(init: UpdateBuilder<State, Output>.() -> Unit): List<Update> {
        ensureNotRunning()
        val builder = UpdateBuilder(transitionCallback)
        builder.init()
        return builder.updates
    }

    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel {
        ensureNotRunning()
        val formulaKey = FormulaKey(formula::class, key)
        if (children.containsKey(formulaKey)) {
            throw IllegalStateException("There already is a child with same key: $formulaKey. Use [key: String] parameter.")
        }

        val result = delegate.child(formula, input, formulaKey, onEvent, processingPass)
        children[formulaKey] = result.updates
        return result.renderModel
    }

    override fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> {
        @Suppress("UNCHECKED_CAST")
        val casted = childBuilder as Child<State, Output, ChildInput, ChildOutput, ChildRenderModel>
        casted.initialize(key, formula)
        return casted
    }

    private fun incrementCallbackCount() {
        callbackCount += 1
    }

    private fun generatedKey() = "formula:generated:$callbackCount"

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}
