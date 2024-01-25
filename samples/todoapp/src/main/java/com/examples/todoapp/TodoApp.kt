package com.examples.todoapp

import android.app.Application
import android.util.Log
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FragmentEnvironment

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(
            application = this,
            fragmentEnvironment = FragmentEnvironment(
                onScreenError = { _, error ->
                    Log.e("TodoApp", "fragment crashed", error)
                }
            ),
            activities = {
                activity<TodoActivity> {
                    val component = TodoAppComponent(this)

                    store(
                        contracts = contracts(component) {
                            bind(TaskListFeatureFactory())
                        }
                    )
                }
            }
        )
    }
}
