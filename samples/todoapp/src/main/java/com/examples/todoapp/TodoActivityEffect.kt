package com.examples.todoapp

sealed class TodoActivityEffect {
    data class ShowToast(val message: String) : TodoActivityEffect()
}
