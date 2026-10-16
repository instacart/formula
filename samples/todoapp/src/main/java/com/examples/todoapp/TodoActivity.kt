package com.examples.todoapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.examples.todoapp.data.TaskRepoImpl
import com.examples.todoapp.tasks.TaskListFeatureFactory
import com.examples.todoapp.tasks.TaskListKey
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId

class TodoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = RouteId(instanceId = "todo-task-list", key = TaskListKey())
        val component = TodoAppComponent { message ->
            onEffect(TodoActivityEffect.ShowToast(message))
        }
        val feature = TaskListFeatureFactory().initialize(component, routeId)
        val state = feature.factory(lifecycleScope)
        val routeDelegate = RouteEnvironment().routeDelegate

        setContent {
            val model by state.collectAsState()
            routeDelegate.Content(routeId, feature.viewFactory, model)
        }
    }

    private fun onEffect(effect: TodoActivityEffect) {
        when (effect) {
            is TodoActivityEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
