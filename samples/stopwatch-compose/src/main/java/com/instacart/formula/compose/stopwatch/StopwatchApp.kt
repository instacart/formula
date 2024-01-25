package com.instacart.formula.compose.stopwatch

import android.app.Application
import android.util.Log
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FragmentEnvironment

class StopwatchApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(
            application = this,
            fragmentEnvironment = FragmentEnvironment(
                onScreenError = { key, error ->
                    Log.e("StopwatchApp", "fragment crashed", error)
                }
            ),
            activities = {
                activity<StopwatchActivity> {
                    store(
                        contracts = contracts(Unit) {
                            bind(StopwatchFeatureFactory())
                        }
                    )
                }
            }
        )
    }
}
