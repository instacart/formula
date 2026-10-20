package com.instacart.formula.android

/**
 * Marker interface describing how a route's render model is rendered by a host.
 *
 * Implementations must extend a known sealed subtype such as [ComposeViewFactory].
 */
sealed interface ViewFactory<RenderModel : Any>
