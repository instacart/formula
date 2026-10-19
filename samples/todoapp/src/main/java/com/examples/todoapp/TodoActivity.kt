package com.examples.todoapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.examples.todoapp.tasks.TaskListScreen
import com.examples.todoapp.tasks.TodoViewModel
import com.instacart.formula.FormulaAndroid
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {

    internal lateinit var todoViewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<TodoViewModel> { todoViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setContent {
            val model by viewModel.outputs.collectAsStateWithLifecycle()
            TaskListScreen(model)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(::onEffect)
            }
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
