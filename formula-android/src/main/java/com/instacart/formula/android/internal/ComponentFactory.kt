package com.instacart.formula.android.internal

import com.instacart.formula.integration.DisposableScope

/**
 * Component factory creates a child component from a parent component.
 */
internal typealias ComponentFactory<ParentComponent, Component> = (ParentComponent) -> DisposableScope<Component>
