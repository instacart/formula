package com.instacart.testutils.android

import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.RouteKey
import org.robolectric.Shadows.shadowOf

fun FragmentActivity.showFragment(
    routeKey: RouteKey,
    allowStateLoss: Boolean = false,
) {
    val entryIndex = supportFragmentManager.backStackEntryCount - 1
    val fragment = if (entryIndex >= 0) {
        val entry = supportFragmentManager.getBackStackEntryAt(entryIndex)
        supportFragmentManager.findFragmentByTag(entry.name)
    } else {
        null
    }

    supportFragmentManager.beginTransaction().apply {
        if (fragment != null) {
            remove(fragment)
        }

        val tag = routeKey.tag
        add(R.id.activity_content, FormulaFragment.newInstance(routeKey), tag)
        addToBackStack(tag)
    }.apply {
        if (allowStateLoss) {
            commitAllowingStateLoss()
        } else {
            commit()
        }
    }
}

fun <ActivityType : FragmentActivity> ActivityScenario<ActivityType>.showFragment(
    routeKey: RouteKey,
    allowStateLoss: Boolean = false
) {
    onActivity {
        it.showFragment(routeKey, allowStateLoss)
        shadowOf(Looper.getMainLooper()).idle()
    }
}
