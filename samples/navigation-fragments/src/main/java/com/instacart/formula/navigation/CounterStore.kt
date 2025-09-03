package com.instacart.formula.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class CounterStore {
    private val _counterIncrements = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _counterList = MutableStateFlow<List<Int>>(emptyList())


    fun counterIncrements(fragmentId: Int): Flow<Unit> {
        return _counterIncrements.filter { it == fragmentId }.map { }
    }

    fun incrementCounterFor(fragmentId: Int) {
        _counterIncrements.tryEmit(fragmentId)
    }

    fun counterStack(): Flow<List<Int>> =_counterList

    fun updateCounterStack(stack: List<Int>) {
        _counterList.value = stack
    }
}