package com.examples.todoapp

import android.app.Application
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore

class TodoApp : Application() {

    private val graph = TodoGraphApp()

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(this) {
            activity<TodoActivity> {
                ActivityStore(
                    configureActivity = { activity ->
                        activity.todoViewModelFactory = graph.viewModelFactory()
                    },
                )
            }
        }
    }
}
