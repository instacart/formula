package com.examples.todoapp

import android.app.Application
import com.examples.todoapp.tasks.TaskListContract
import com.examples.todoapp.tasks.TaskListFormula
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.start
import com.instacart.formula.state

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FormulaAndroid.init(this) {
            activity<TodoActivity> {
                val component = TodoAppComponent()
                store(
                    contracts = contracts(component) {
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
    }
}
