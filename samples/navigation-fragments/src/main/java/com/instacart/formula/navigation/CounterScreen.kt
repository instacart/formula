package com.instacart.formula.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CounterScreen(renderModel: CounterFragmentRenderModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // Fragment title
        Text(
            text = "Fragment ${renderModel.fragmentId}",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        // Counter text
        Text(
            text = "Counter: ${renderModel.counter}",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        // Navigate next button
        Button(
            onClick = { renderModel.onNavigateToNext() },
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Text("Navigate to Next Fragment")
        }
        // Navigate back button (conditionally shown)
        if (renderModel.backStackFragments.size > 1) {
            Button(
                onClick = { renderModel.onNavigateBack() },
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text("Navigate Back")
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Fragment counters
        if (renderModel.backStackFragments.isNotEmpty()) {
            Text(
                text = "Fragment Counters:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                renderModel.backStackFragments.forEach { fragmentId ->
                    Button(
                        onClick = { renderModel.onIncrementCounter(fragmentId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Increment Counter for Fragment $fragmentId")
                    }
                }
            }
        }
    }
}
