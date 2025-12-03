package com.examples.todoapp

import android.app.Application
import android.util.Log
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.NavigationStore

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val routeEnvironment = RouteEnvironment(
            onScreenError = { _, error ->
                Log.e("TodoApp", "fragment crashed", error)
            }
        )

        FormulaAndroid.init(
            application = this,
            activities = {
                activity<TodoActivity> {
                    val component = TodoAppComponent(this)

                    ActivityStore(
                        navigationStore = NavigationStore.Builder()
                            .setRouteEnvironment(routeEnvironment)
                            .build(component) {
                                bind(TaskListFeatureFactory())
                            }
                    )
                }
            }
        )
    }
}
