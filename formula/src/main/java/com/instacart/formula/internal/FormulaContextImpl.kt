package com.instacart.formula.internal

import com.instacart.formula.Child
import com.instacart.formula.FormulaContext
import com.instacart.formula.Formula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State, Output> internal constructor(
    private val processingPass: Long,
    private val callbacks: ScopedCallbacks,
    private val delegate: Delegate<State, Output>,
    private val transitionCallback: TransitionCallbackWrapper<State, Output>
) : FormulaContext<State, Output>() {

    private var childBuilder: Child<State, Output, *, *, *> = Child<State, Output, Any, Any, Any>(this)

    interface Delegate<State, Effect> {
        fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
            input: ChildInput,
            key: Any,
            onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>,
            processingPass: Long
        ): ChildRenderModel
    }

    override fun performTransition(transition: Transition<State, Output>) {
        transitionCallback.invoke(transition)
    }

    override fun initOrFindPositionalCallback(): Callback {
        ensureNotRunning()
        return callbacks.initOrFindPositionalCallback()
    }

    override fun initOrFindOptionalCallback(condition: Boolean): Callback? {
        return callbacks.initOrFindOptionalCallback(condition)
    }

    override fun initOrFindCallback(key: String): Callback {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        return callbacks.initOrFindCallback(key)
    }

    override fun <UIEvent> initOrFindPositionalEventCallback(): EventCallback<UIEvent> {
        ensureNotRunning()
        return callbacks.initOrFindPositionalEventCallback()
    }

    override fun <UIEvent> initOrFindOptionalEventCallback(condition: Boolean): EventCallback<UIEvent>? {
        return callbacks.initOrFindOptionalEventCallback(condition)
    }

    override fun <UIEvent> initOrFindEventCallback(key: String): EventCallback<UIEvent> {
        ensureNotRunning()

        if (key.isBlank()) {
            throw IllegalStateException("Key cannot be blank.")
        }

        return callbacks.initOrFindEventCallback<UIEvent>(key)
    }

    override fun updates(init: UpdateBuilder<State, Output>.() -> Unit): List<Update> {
        ensureNotRunning()
        val builder = UpdateBuilder(transitionCallback)
        builder.init()
        return builder.updates
    }

    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: Any,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel {
        ensureNotRunning()
        return delegate.child(formula, input, key, onEvent, processingPass)
    }

    override fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> {
        ensureNotRunning()

        @Suppress("UNCHECKED_CAST")
        val casted = childBuilder as Child<State, Output, ChildInput, ChildOutput, ChildRenderModel>
        casted.initialize(key, formula)
        return casted
    }

    @PublishedApi internal fun <Value> key(key: Any, create: () -> Value): Value {
        callbacks.enterScope(key)
        val value = create()
        callbacks.endScope()
        return value
    }

    private fun ensureNotRunning() {
        if (transitionCallback.running) {
            throw IllegalStateException("cannot call this after evaluation finished.")
        }
    }
}
