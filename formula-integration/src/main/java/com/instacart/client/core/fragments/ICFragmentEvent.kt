package com.instacart.client.core.fragments

import androidx.fragment.app.Fragment


sealed class ICFragmentEvent(val fragment: Fragment) {
    class Attached(fragment: Fragment): ICFragmentEvent(fragment)
    class Detached(fragment: Fragment): ICFragmentEvent(fragment)
}