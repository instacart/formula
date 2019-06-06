package com.examples.todoapp

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.examples.todoapp.tasks.TaskListContract
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.fragment.FormulaFragment

class TodoActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_activity)

        if (savedInstanceState == null) {
            val contract = TaskListContract()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(contract), contract.tag)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (!FormulaAndroid.onBackPressed(this)) {
            super.onBackPressed()
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
