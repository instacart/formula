package com.examples.todoapp

import android.app.Application
import android.util.Log
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.start

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
                    val component = TodoAppComponent()

                    store(
                        fragments = fragments(component) {
                            bind(TaskListContract::class) { component, key ->
                                val input = TaskListFormula.Input(showToast = { message ->
                                    send {
                                        onEffect(TodoActivityEffect.ShowToast(message))
                                    }
                                })

                                component.createTaskListFormula().start(input)
                            }
                        }
                    )
                }
            }
        )
    }
}
