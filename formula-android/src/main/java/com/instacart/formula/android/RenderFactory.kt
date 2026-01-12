package com.instacart.formula.android

/**
 * Base interface for factories that render [RenderModel] to a UI.
 *
 * Two implementations exist:
 * - [ViewFactory] - For Fragment-based rendering with Android Views
 * - [com.instacart.formula.android.compose.ComposeRenderFactory] - For Nav3 Compose rendering (in formula-android-compose module)
 *
 * This interface allows [Feature] to work with both Fragment and Nav3 navigation modes
 * without being tied to a specific rendering mechanism.
 */
interface RenderFactory<RenderModel>
