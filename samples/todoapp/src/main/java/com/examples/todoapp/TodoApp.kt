package com.examples.todoapp

import android.app.Application
import android.util.Log
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentStore

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val fragmentEnvironment = FragmentEnvironment(
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
                        fragmentStore = FragmentStore.Builder()
                            .setFragmentEnvironment(fragmentEnvironment)
                            .build(component) {
                                bind(TaskListFeatureFactory())
                            }
                    )
                }
            }
        )
    }
}
