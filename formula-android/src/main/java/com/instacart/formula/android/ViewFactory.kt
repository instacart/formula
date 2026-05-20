package com.instacart.formula.android

/**
 * Marker interface for a route's renderable surface. Used by [FormulaFragment]
 * (and Compose-native hosts) to dispatch over the sealed hierarchy.
 *
 * Implementations must extend a known sealed subtype such as [ComposeViewFactory].
 */
sealed interface ViewFactory<RenderModel : Any>
