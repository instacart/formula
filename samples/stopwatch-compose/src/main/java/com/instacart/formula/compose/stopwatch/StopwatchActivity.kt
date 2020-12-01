package com.instacart.formula.compose.stopwatch

import android.os.Bundle
import android.text.Layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

class StopwatchActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { render(StopwatchFormula().toObservable()) }
    }
}

@Composable
fun render(state: Observable<StopwatchRenderModel>) {
    val model = state.subscribeAsState(null).value
    if (model != null) {
        MaterialTheme {
            Surface {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Box(alignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = model.timePassed, color = Color.Black)
                            Spacer(Modifier.size(Dp(8f)))
                            Row {
                                render(model.startStopButton)
                                Spacer(Modifier.size(Dp(8f)))
                                render(model.resetButton)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun render(model: ButtonRenderModel) {
    Button(
        onClick = model.onSelected,
        colors = ButtonConstants.defaultButtonColors(
            backgroundColor = Color.Gray,
        ),
        content = {
            Text(text = model.text, color = Color.White)
        }
    )
}