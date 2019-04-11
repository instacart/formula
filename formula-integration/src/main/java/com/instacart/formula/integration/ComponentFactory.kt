package com.instacart.formula.integration

/**
 * Component factory creates a child component from a parent component.
 */
typealias ComponentFactory<ParentComponent, Component> = (ParentComponent) -> DisposableScope<Component>
