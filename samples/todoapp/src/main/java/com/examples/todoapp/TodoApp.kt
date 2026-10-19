package com.examples.todoapp

import android.app.Application
import com.examples.todoapp.data.TaskRepoImpl
import com.examples.todoapp.tasks.TodoViewModel
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.ActivityStore

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(this) {
            activity<TodoActivity> {
                ActivityStore(
                    configureActivity = { activity ->
                        activity.todoViewModelFactory = TodoViewModel.Factory(TaskRepoImpl())
                    },
                )
            }
        }
    }
}
