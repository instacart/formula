package com.instacart.client.mvi

import androidx.fragment.app.FragmentActivity
import com.instacart.client.core.fragments.ICFragmentEvent
import com.instacart.client.core.fragments.ICRxFragment
import com.instacart.client.core.rx.mapNotNull
import io.reactivex.Flowable

/**
 * Helps activities track active MVI contracts.
 */
object ICMviActivity {

    /**
     * Must subscribe to the state before calling Activity.super.onCreate(),
     * otherwise you might miss fragment event
     */
    @JvmStatic fun lifecycleEffects(
        activity: FragmentActivity
    ): Flowable<ICMviLifecycleEvent<ICMviFragmentContract<*>>> {
        return ICRxFragment
            .fragmentLifecycleEvents(activity)
            .mapNotNull { event ->
                val fragment = event.fragment as? ICBaseMviFragment<*>
                val contract = fragment?.getMviContract() ?: ICEmptyMviFragmentContract(event.fragment.tag.orEmpty())
                contract.let { it: ICMviFragmentContract<*> ->
                    when (event) {
                        is ICFragmentEvent.Attached -> ICMviLifecycleEvent.Attach(it)
                        is ICFragmentEvent.Detached -> {
                            // Only trigger detach, when fragment is actually being removed from the backstack
                            if (event.fragment.isRemoving) {
                                ICMviLifecycleEvent.Detach(it, fragment?.currentState())
                            } else {
                                null
                            }
                        }
                    }
                }
            }
    }
}
