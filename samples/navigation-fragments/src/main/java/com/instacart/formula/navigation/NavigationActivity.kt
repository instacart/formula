package com.instacart.formula.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FormulaFragment

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        if (savedInstanceState == null) {
            showNextFragment(id = 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FormulaAndroid.onActivityResult(this, requestCode, resultCode, data)
    }

    fun showNextFragment(id: Int) {
        val key = CounterRouteKey(id)
        val fragment = FormulaFragment.newInstance(key)

        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.activity_content, fragment, key.tag)
            addToBackStack(key.tag)
            commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
