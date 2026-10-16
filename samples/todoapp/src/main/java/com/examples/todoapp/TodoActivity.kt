package com.examples.todoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.examples.todoapp.tasks.TaskListKey
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FormulaFragment

class TodoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_activity)

        if (savedInstanceState == null) {
            val key = TaskListKey()
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, FormulaFragment.newInstance(key), key.tag)
                .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FormulaAndroid.onActivityResult(this, requestCode, resultCode, data)
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
