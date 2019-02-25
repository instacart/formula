package com.instacart.formula.integration

import org.junit.Test

class BackstackStoreTest {

    @Test fun lifecycleEvent_attach() {
        val machine = BackstackStore<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(LifecycleEvent.Attach("my-key"))
            }
            .assertValues(
                ActiveKeys.empty(),
                ActiveKeys(listOf("my-key"))
            )
    }

    @Test fun lifecycleEvent_detach() {
        val machine = BackstackStore(listOf("my-key"))
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(LifecycleEvent.Detach("my-key"))
            }
            .assertValues(
                ActiveKeys(listOf("my-key")),
                ActiveKeys.empty()
            )
    }

    @Test fun navigateTo_multipleEvents() {
        val machine = BackstackStore<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.navigateTo("first-key")
                machine.navigateTo("second-key")
            }
            .assertValues(
                ActiveKeys.empty(),
                ActiveKeys(listOf("first-key")),
                ActiveKeys(listOf("first-key", "second-key"))
            )
    }

    @Test fun close() {
        val machine = BackstackStore(listOf("first-key"))
        machine.stateChanges().test()
            .apply {
                machine.close("first-key")
            }
            .assertValues(
                ActiveKeys(listOf("first-key")),
                ActiveKeys.empty()
            )
    }

    @Test fun navigateBack_hasItemsInBackstack() {
        val machine = BackstackStore("first-key")
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                ActiveKeys(listOf("first-key")),
                ActiveKeys.empty()
            )
    }

    @Test fun navigateBack_empty() {
        val machine = BackstackStore<String>()
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                ActiveKeys.empty()
            )
    }
}
