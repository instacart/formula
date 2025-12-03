package com.instacart.formula.compose.stopwatch

import android.app.Application
import android.util.Log
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.NavigationStore

class StopwatchApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val routeEnvironment = RouteEnvironment(
            onScreenError = { _, error ->
                Log.e("StopwatchApp", "fragment crashed", error)
            }
        )
        FormulaAndroid.init(
            application = this,
            activities = {
                activity<StopwatchActivity> {
                    ActivityStore(
                        navigationStore = NavigationStore.Builder()
                            .setRouteEnvironment(routeEnvironment)
                            .build {
                                bind(StopwatchFeatureFactory())
                            }
                    )
                }
            }
        )
    }
}
