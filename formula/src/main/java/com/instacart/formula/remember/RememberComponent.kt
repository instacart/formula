package com.instacart.formula.remember

import com.instacart.formula.internal.LifecycleComponent

@PublishedApi
internal class RememberComponent<T>(val value: T) : LifecycleComponent