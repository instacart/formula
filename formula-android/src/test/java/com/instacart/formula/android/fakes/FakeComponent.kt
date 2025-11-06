package com.instacart.formula.android.fakes

import com.instacart.formula.android.RouteKey
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class FakeComponent {
    val updateRelay: PublishRelay<Pair<RouteKey, String>> = PublishRelay.create()

    fun state(key: RouteKey): Observable<String> {
        val updates = updateRelay.filter { it.first == key }.map { it.second }
        return updates.startWithItem("${key.tag}-state")
    }
}