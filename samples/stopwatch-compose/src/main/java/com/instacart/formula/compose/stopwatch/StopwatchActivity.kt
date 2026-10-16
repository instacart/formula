package com.instacart.formula.compose.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId

class StopwatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = RouteId(instanceId = "stopwatch", key = StopwatchKey())
        val feature = StopwatchFeatureFactory().initialize(Any(), routeId)
        val state = feature.factory(lifecycleScope)
        val routeDelegate = RouteEnvironment().routeDelegate

        setContent {
            val model by state.collectAsState()
            routeDelegate.Content(routeId, feature.viewFactory, model)
        }
    }
}
