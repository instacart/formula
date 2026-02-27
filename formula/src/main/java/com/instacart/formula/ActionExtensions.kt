package com.instacart.formula

import com.instacart.formula.action.ActionKey
import com.instacart.formula.action.OverrideKeyAction

/**
 * Wraps this action with a new key to force it to run again.
 */
fun <Event> Action<Event>.runAgain(): Action<Event> {
    return overrideKey(key = newActionKey(this, this))
}

/**
 * Wraps the action with a unique key to ensure that [previousAction] is cancelled and
 * this action runs.
 */
fun <Event> Action<Event>.cancelPrevious(previousAction: Action<Event>?): Action<Event> {
    return overrideKey(key = newActionKey(previousAction, this))
}

private fun <Event> Action<Event>.overrideKey(key: Any): Action<Event> {
    val unwrappedAction = if (this is OverrideKeyAction) {
        this.action
    } else {
        this
    }

    return OverrideKeyAction(key, unwrappedAction)
}

private fun newActionKey(previous: Action<*>?, new: Action<*>): ActionKey {
    val previousKey = previous?.key() as? ActionKey
    val id = if (previousKey != null) {
        previousKey.id + 1
    } else {
        0
    }
    return ActionKey(id, new.key())
}