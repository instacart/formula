package com.instacart.formula.compose.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.compose.ComposeViewFactory
import com.instacart.formula.rxjava3.toObservable

class StopwatchFeatureFactory : FeatureFactory<Any, StopwatchKey> {
    override fun initialize(dependencies: Any, key: StopwatchKey): Feature<*> {
        return Feature(
            state = StopwatchFormula().toObservable(),
            viewFactory = StopwatchViewFactory()
        )
    }
}

class StopwatchViewFactory : ComposeViewFactory<StopwatchRenderModel>() {
    @Composable
    override fun Content(model: StopwatchRenderModel) {
        MaterialTheme {
            Surface {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = model.timePassed, color = Color.Black)
                            Spacer(Modifier.size(Dp(8f)))
                            Row {
                                StopwatchButton(model.startStopButton)
                                Spacer(Modifier.size(Dp(8f)))
                                StopwatchButton(model.resetButton)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StopwatchButton(model: ButtonRenderModel) {
        Button(
            onClick = model.onSelected,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Gray,
            ),
            content = {
                Text(text = model.text, color = Color.White)
            }
        )
    }
}
