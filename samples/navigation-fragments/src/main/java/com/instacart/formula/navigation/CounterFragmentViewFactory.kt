package com.instacart.formula.navigation

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import com.instacart.formula.android.compose.ComposeViewFactory

class CounterFragmentViewFactory : ComposeViewFactory<CounterFragmentRenderModel>() {

    @Composable
    override fun Content(model: CounterFragmentRenderModel) {
        MaterialTheme {
            Surface {
                CounterScreen(model)
            }
        }
    }
}