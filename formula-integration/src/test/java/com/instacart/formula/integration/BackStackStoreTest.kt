package com.instacart.formula.integration

import org.junit.Test

class BackStackStoreTest {

    @Test fun lifecycleEvent_attach() {
        val machine = BackStackStore<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(LifecycleEvent.Added("my-key"))
            }
            .assertValues(
                BackStack.empty(),
                BackStack(listOf("my-key"))
            )
    }

    @Test fun lifecycleEvent_detach() {
        val machine = BackStackStore(listOf("my-key"))
        machine
            .stateChanges()
            .test()
            .apply {
                machine.onLifecycleEffect(LifecycleEvent.Removed("my-key"))
            }
            .assertValues(
                BackStack(listOf("my-key")),
                BackStack.empty()
            )
    }

    @Test fun navigateTo_multipleEvents() {
        val machine = BackStackStore<String>()
        machine
            .stateChanges()
            .test()
            .apply {
                machine.navigateTo("first-key")
                machine.navigateTo("second-key")
            }
            .assertValues(
                BackStack.empty(),
                BackStack(listOf("first-key")),
                BackStack(listOf("first-key", "second-key"))
            )
    }

    @Test fun close() {
        val machine = BackStackStore(listOf("first-key"))
        machine.stateChanges().test()
            .apply {
                machine.close("first-key")
            }
            .assertValues(
                BackStack(listOf("first-key")),
                BackStack.empty()
            )
    }

    @Test fun navigateBack_hasItemsInBackstack() {
        val machine = BackStackStore("first-key")
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                BackStack(listOf("first-key")),
                BackStack.empty()
            )
    }

    @Test fun navigateBack_empty() {
        val machine = BackStackStore<String>()
        machine.stateChanges().test()
            .apply {
                machine.navigateBack()
            }
            .assertValues(
                BackStack.empty()
            )
    }
}
