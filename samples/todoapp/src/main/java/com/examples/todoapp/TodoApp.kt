package com.examples.todoapp

import android.app.Application
import android.util.Log
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.instacart.formula.FormulaAndroid

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(
            application = this,
            onFragmentError = { contract, error ->
                Log.e("TodoApp", "fragment crashed", error)
            },
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
