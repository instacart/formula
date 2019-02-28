package androidx.fragment.app

import android.view.View

object FragmentInspector {
    fun isHeadless(fragment: Fragment): Boolean {
        return fragment.mContainerId == View.NO_ID || fragment.mContainerId == 0
    }
}
