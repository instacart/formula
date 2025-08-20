package com.instacart.formula.navigation

import android.app.Application
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentStore

class NavigationApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(
            application = this,
            activities = {
                activity<NavigationActivity> {
                    val activityComponent = NavigationActivityComponent(this)
                    ActivityStore(
                        fragmentStore = FragmentStore.Builder()
                            .build(activityComponent) {
                                bind(CounterFragmentFeatureFactory())
                            },
                    )
                }
            },
        )
    }
}