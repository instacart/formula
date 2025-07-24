package com.instacart.formula.navigation

import android.app.Application
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentStore

class NavigationApp : Application() {

    companion object {
        lateinit var navigationStore: NavigationStore
        var onNavigationEffect: ((NavigationEffect) -> Unit)? = null
    }

    override fun onCreate() {
        super.onCreate()

        navigationStore = NavigationStore()

        FormulaAndroid.init(
            application = this,
            activities = {
                activity<NavigationActivity> {
                    ActivityStore(
                        fragmentStore = FragmentStore.Builder()
                            .build {
                                bind(NavigationFragmentFeatureFactory(navigationStore) { effect ->
                                    onNavigationEffect?.invoke(effect)
                                })
                            }
                    )
                }
            }
        )
    }
}