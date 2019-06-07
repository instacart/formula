package com.examples.todoapp

import android.app.Application
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.FormulaAndroid

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(this) {
            activity(TodoActivity::class) { effectHandler ->
                val component = TodoAppComponent()

                build(component) {
                    bind(TaskListContract::class) { component, key ->
                        val input = TaskListFormula.Input(showToast = { message ->
                            effectHandler.send {
                                onEffect(TodoActivityEffect.ShowToast(message))
                            }
                        })

                        component.createTaskListFormula().state(input)
                    }
                }
            }
        }
    }
}
