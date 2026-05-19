package com.instacart.formula.android

import androidx.compose.runtime.Composable

/**
 * Describes how a Formula feature renders. Returned by [ViewFactory.create] and consumed by
 * [FormulaFragment] (which hosts the [content] in a `ComposeView`) or by Compose-native hosts
 * (which invoke [content] directly).
 *
 * @param content Composable that renders the latest [RenderModel].
 * @param initialModel Optional initial model rendered before the first state emission.
 */
class FeatureView<RenderModel>(
    val content: @Composable (RenderModel) -> Unit,
    val initialModel: RenderModel? = null,
)
