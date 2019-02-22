package com.instacart.client.mvi

import android.os.Bundle


interface ICFragmentLifecycleCallback {
    fun onStart() = Unit
    fun onResume() = Unit
    fun onPause() = Unit
    fun onStop() = Unit
    fun onSaveInstanceState(outState: Bundle) = Unit
    fun onLowMemory() = Unit
    fun onDestroyView() = Unit
}
