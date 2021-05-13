package com.examples.todoapp

import android.os.Bundle
import android.widget.Toast
import com.examples.todoapp.tasks.TaskListKey
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.android.FormulaAppCompatActivity

class TodoActivity : FormulaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_activity)

        if (savedInstanceState == null) {
            val key = TaskListKey()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(key), key.tag)
                .commit()
        }
    }

    fun onEffect(effect: TodoActivityEffect) {
        when (effect) {
            is TodoActivityEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
