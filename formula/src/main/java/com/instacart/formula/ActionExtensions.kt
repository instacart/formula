package com.instacart.formula

import com.instacart.formula.internal.ActionWithId


/**
 * Creates a deferred action with a new key which will force it to run again.
 */
fun <Event> Action<Event>.runAgain(): Action<Event> {
    val previous = findActionWithId()
    return ActionWithId.create(
        previous = previous,
        delegateAction = previous?.delegateAction ?: this
    )
}

/**
 * Creates a deferred action with an updated key which will force it to run again. It
 * uses [factory] to instantiate the action instead of relying on the receiver [action][this].
 */
inline fun <Event> Action<Event>?.runAgain(
    factory: () -> Action<Event>
): Action<Event> {
    val previous = findActionWithId()
    return ActionWithId.create(previous, factory())
}

@PublishedApi
internal fun <Event> Action<Event>?.findActionWithId(): ActionWithId<Event>? {
    return if (this is DelegateAction) {
        this.action.findActionWithId()
    } else {
        this as? ActionWithId
    }
}