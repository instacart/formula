package com.instacart.formula.android.fakes

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentKey
import com.instacart.formula.integration.DisposableScope
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class FakeComponent {
    val initialized = mutableListOf<Pair<FakeAuthFlowFactory.Component, FragmentContract<*>>>()
    val updateRelay: PublishRelay<Pair<FragmentKey, String>> = PublishRelay.create()

    fun state(key: FragmentKey): Observable<String> {
        val updates = updateRelay.filter { it.first == key }.map { it.second }
        return updates.startWithItem("${key.tag}-state")
    }

    fun createAuthFlowComponent(): DisposableScope<FakeAuthFlowFactory.Component> {
        val component = FakeAuthFlowFactory.Component(onInitialized = { component, key ->
            initialized.add(component to key)
        })
        return DisposableScope(component, {
            initialized.clear()
        })
    }
}