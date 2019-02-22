package com.instacart.client.mvi.backstack

import com.instacart.client.mvi.ICActiveMviKeys
import com.instacart.client.mvi.ICMviLifecycleEvent
import org.junit.Test

class ICStateMachineTest {

    @Test fun lifecycleEvent_attach() {
        val machine = ICStateMachine<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(ICMviLifecycleEvent.Attach("my-key"))
            }
            .assertValues(
                ICActiveMviKeys.empty(),
                ICActiveMviKeys(listOf("my-key"))
            )
    }

    @Test fun lifecycleEvent_detach() {
        val machine = ICStateMachine(listOf("my-key"))
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(ICMviLifecycleEvent.Detach("my-key"))
            }
            .assertValues(
                ICActiveMviKeys(listOf("my-key")),
                ICActiveMviKeys.empty()
            )
    }

    @Test fun navigateTo_multipleEvents() {
        val machine = ICStateMachine<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.navigateTo("first-key")
                machine.navigateTo("second-key")
            }
            .assertValues(
                ICActiveMviKeys.empty(),
                ICActiveMviKeys(listOf("first-key")),
                ICActiveMviKeys(listOf("first-key", "second-key"))
            )
    }

    @Test fun close() {
        val machine = ICStateMachine(listOf("first-key"))
        machine.stateChanges().test()
            .apply {
                machine.close("first-key")
            }
            .assertValues(
                ICActiveMviKeys(listOf("first-key")),
                ICActiveMviKeys.empty()
            )
    }

    @Test fun navigateBack_hasItemsInBackstack() {
        val machine = ICStateMachine("first-key")
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                ICActiveMviKeys(listOf("first-key")),
                ICActiveMviKeys.empty()
            )
    }

    @Test fun navigateBack_empty() {
        val machine = ICStateMachine<String>()
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                ICActiveMviKeys.empty()
            )
    }
}
