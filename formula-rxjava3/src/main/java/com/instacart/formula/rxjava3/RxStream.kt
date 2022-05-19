@file:Suppress("unused")

package com.instacart.formula.rxjava3

@Deprecated(
    "use RxAction",
    replaceWith = ReplaceWith("RxAction", "com.instacart.formula.rxjava3.RxAction")
)
typealias RxStream<Event> = RxAction<Event>
