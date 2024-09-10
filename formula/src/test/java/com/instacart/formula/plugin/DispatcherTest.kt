package com.instacart.formula.plugin

import com.google.common.truth.Truth
import com.instacart.formula.subjects.TestDispatcherPlugin
import org.junit.Test

class DispatcherTest {
    @Test fun `main dispatcher delegates to plugin`() {
        val plugin = TestDispatcherPlugin()
        withPlugin(plugin) {
            Dispatcher.Main.dispatch {  }
            plugin.mainDispatcher.assertCalled(1)

            Dispatcher.Main.isDispatchNeeded()
            Truth.assertThat(plugin.mainDispatcher.isDispatchNeededCalled.get()).isEqualTo(1)
        }
    }
}