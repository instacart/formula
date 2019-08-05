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
    val callbacks = mutableSetOf<Any>()
    val eventCallbacks = mutableSetOf<Any>()

    interface Delegate<State, Effect> {
        fun initOrFindCallback(key: Any): Callback

        fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent>

        fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>,
            processingPass: Long
        ): Evaluation<ChildRenderModel>
    }

    override fun performTransition(transition: Transition<State, Output>) {
        transitionCallback.invoke(transition)
    }

    override fun initOrFindPositionalCallback(): Callback {
        ensureNotRunning()
        val key = callbackCount
        callbacks.add(key)
        val callback = delegate.initOrFindCallback(key)
        incrementCallbackCount()
        return callback
    }

    override fun initOrFindOptionalCallback(condition: Boolean): Callback? {
        return if (condition) {
            initOrFindPositionalCallback()
        } else {
            incrementCallbackCount()
            null
        }
    }

    override fun initOrFindCallback(key: String): Callback {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        if (callbacks.contains(key)) {
            throw IllegalStateException("Callback $key is already defined. Make sure your key is unique.")
        }

        callbacks.add(key)
        return delegate.initOrFindCallback(key)
    }

    override fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent> {
        ensureNotRunning()
        val key = callbackCount
        eventCallbacks.add(key)
        incrementCallbackCount()
        return delegate.initOrFindEventCallback(key)
    }

    override fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>? {
        return if (condition) {
            initOrFindPositionalEventCallback()
        } else {
            incrementCallbackCount()
            null
        }
    }

    override fun <UIEvent> initOrFindEventCallback(key: String): EventCallback<UIEvent> {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        if (eventCallbacks.contains(key)) {
            throw IllegalStateException("Event callback $key is already defined. Make sure your key is unique.")
        }

        eventCallbacks.add(key)
        return delegate.initOrFindEventCallback<UIEvent>(key)
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

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}
