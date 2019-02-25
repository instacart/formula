package com.instacart.formula.fragment

import androidx.fragment.app.Fragment


sealed class FragmentEvent(val fragment: Fragment) {
    class Attached(fragment: Fragment): FragmentEvent(fragment)
    class Detached(fragment: Fragment): FragmentEvent(fragment)
}
