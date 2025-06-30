package com.instacart.formula.compose.stopwatch

import android.app.Application
import android.util.Log
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentStore

class StopwatchApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val fragmentEnvironment = FragmentEnvironment(
            onScreenError = { _, error ->
                Log.e("StopwatchApp", "fragment crashed", error)
            }
        )
        FormulaAndroid.init(
            application = this,
            activities = {
                activity<StopwatchActivity> {
                    ActivityStore(
                        fragmentStore = FragmentStore.Builder()
                            .setFragmentEnvironment(fragmentEnvironment)
                            .build {
                                bind(StopwatchFeatureFactory())
                            }
                    )
                }
            }
        )
    }
}
