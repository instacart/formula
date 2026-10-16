package com.instacart.formula.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CounterScreen(model: CounterRenderModel) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { model.onDecrement(Unit) }) {
                    Text("-")
                }
                Text(
                    text = model.count,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Button(onClick = { model.onIncrement(Unit) }) {
                    Text("+")
                }
            }
        }
    }
}
