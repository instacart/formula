package com.instacart.formula.android.fakes

import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.events.FragmentLifecycleEvent

fun FragmentKey.asAddedEvent(instanceId: String = "") = FragmentLifecycleEvent.Added(
    fragmentId = FragmentId(instanceId, this)
)