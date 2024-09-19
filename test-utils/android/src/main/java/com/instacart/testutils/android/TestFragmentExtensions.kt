package com.instacart.testutils.android

import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentKey
import org.robolectric.Shadows.shadowOf

fun FragmentActivity.showFragment(
    fragmentKey: FragmentKey,
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

        val tag = fragmentKey.tag
        add(R.id.activity_content, FormulaFragment.newInstance(fragmentKey), tag)
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
    fragmentKey: FragmentKey,
    allowStateLoss: Boolean = false
) {
    onActivity {
        it.showFragment(fragmentKey, allowStateLoss)
        shadowOf(Looper.getMainLooper()).idle()
    }
}
