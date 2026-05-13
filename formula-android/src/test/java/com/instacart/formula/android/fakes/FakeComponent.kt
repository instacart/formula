package com.instacart.formula.android.fakes

import com.instacart.formula.android.RouteKey
import kotlinx.coroutines.flow.MutableStateFlow

class FakeComponent {
    private val states = mutableMapOf<RouteKey, MutableStateFlow<String>>()

    fun getOrCreateState(key: RouteKey): MutableStateFlow<String> {
        return states.getOrPut(key) { MutableStateFlow("${key.tag}-state") }
    }

    fun updateState(key: RouteKey, value: String) {
        getOrCreateState(key).value = value
    }
}
