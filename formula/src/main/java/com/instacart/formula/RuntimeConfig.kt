package com.instacart.formula

import com.instacart.formula.plugin.Inspector

class RuntimeConfig(
    val inspector: Inspector? = null,
    val isValidationEnabled: Boolean = false,
)
