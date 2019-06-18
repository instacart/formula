package com.instacart.formula

import android.app.Activity
import androidx.test.core.app.ActivityScenario

fun <A: Activity> ActivityScenario<A>.activity(): A {
    return get { this }
}

fun <A: Activity, T> ActivityScenario<A>.get(select: A.() -> T): T {
    val list: MutableList<T> = mutableListOf()
    onActivity {
        list.add(it.select())
    }
    return list.first()
}
