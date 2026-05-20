package com.instacart.formula.android.compose

/**
 * Backwards-compatible alias for the previous location of [ComposeViewFactory] in
 * the now-folded `formula-android-compose` module. Allows downstream consumers
 * that imported from `com.instacart.formula.android.compose` to upgrade without
 * touching imports for one release. Slated for removal in one of the upcoming releases.
 */
@Deprecated(
    message = "Moved to com.instacart.formula.android.ComposeViewFactory",
    replaceWith = ReplaceWith(
        "ComposeViewFactory",
        "com.instacart.formula.android.ComposeViewFactory",
    ),
)
typealias ComposeViewFactory<R> = com.instacart.formula.android.ComposeViewFactory<R>
