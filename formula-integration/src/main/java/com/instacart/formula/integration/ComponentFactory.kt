package com.instacart.formula.integration

typealias ComponentFactory<ParentComponent, Component> = (ParentComponent) -> DisposableScope<Component>
