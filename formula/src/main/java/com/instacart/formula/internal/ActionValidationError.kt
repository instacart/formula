package com.instacart.formula.internal

sealed class ActionValidationError {

    class NewAction(val newActionKey: Any): ActionValidationError()
    class RemovedAction(val removedActionKey: Any): ActionValidationError()
}