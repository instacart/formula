package com.instacart.formula.fragment

import android.os.Bundle


interface FragmentLifecycleCallback {
    fun onStart() = Unit
    fun onResume() = Unit
    fun onPause() = Unit
    fun onStop() = Unit
    fun onSaveInstanceState(outState: Bundle) = Unit
    fun onLowMemory() = Unit
    fun onDestroyView() = Unit
}
