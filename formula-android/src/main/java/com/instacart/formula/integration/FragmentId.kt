package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentKey

/**
 * An object used to identify a fragment. It combines both a user generated [key] and
 * a generated [String] id.
 *
 * @param instanceId Unique identifier used to distinguish a fragment. Since there
 * can be multiple fragments with the same [key], we use this as an extra identifier.
 *
 * @param key Fragment key used to create this fragment.
 */
data class FragmentId(
    val instanceId: String,
    val key: FragmentKey
)