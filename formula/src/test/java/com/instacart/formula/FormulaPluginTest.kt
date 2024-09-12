package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.ClearPluginsRule
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Plugin
import com.instacart.formula.subjects.IncrementingDispatcher
import org.junit.Rule
import org.junit.Test

class FormulaPluginTest {

    private class TestPlugin(
        private val mainDispatcher: Dispatcher? = null,
        private val backgroundDispatcher: Dispatcher? = null,
        private val defaultDispatcher: Dispatcher? = null,
    ) : Plugin {
        override fun mainThreadDispatcher(): Dispatcher? {
            return mainDispatcher ?: super.mainThreadDispatcher()
        }

        override fun backgroundThreadDispatcher(): Dispatcher? {
            return backgroundDispatcher ?: super.backgroundThreadDispatcher()
        }

        override fun defaultDispatcher(): Dispatcher? {
            return defaultDispatcher ?: super.defaultDispatcher()
        }
    }

    @get:Rule
    val rule = ClearPluginsRule()

    @Test fun `when no plugin is specified, default dispatcher is none`() {
        assertThat(FormulaPlugins.defaultDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `when plugin returns null, default dispatcher is none`() {
        FormulaPlugins.setPlugin(TestPlugin())
        assertThat(FormulaPlugins.defaultDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `plugin can override default dispatcher`() {
        val myDispatcher = IncrementingDispatcher()
        FormulaPlugins.setPlugin(TestPlugin(defaultDispatcher = myDispatcher))
        assertThat(FormulaPlugins.defaultDispatcher()).isEqualTo(myDispatcher)
    }

    @Test fun `when no plugin is specified, default background dispatcher is none`() {
        assertThat(FormulaPlugins.backgroundThreadDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `when plugin returns null, default background dispatcher is none`() {
        FormulaPlugins.setPlugin(TestPlugin())
        assertThat(FormulaPlugins.backgroundThreadDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `plugin can override background dispatcher`() {
        val myDispatcher = IncrementingDispatcher()
        FormulaPlugins.setPlugin(TestPlugin(backgroundDispatcher = myDispatcher))
        assertThat(FormulaPlugins.backgroundThreadDispatcher()).isEqualTo(myDispatcher)
    }

    @Test fun `when plugin is null, default main dispatcher is none`() {
        assertThat(FormulaPlugins.mainThreadDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `when plugin returns null, default main dispatcher is none`() {
        FormulaPlugins.setPlugin(TestPlugin())
        assertThat(FormulaPlugins.mainThreadDispatcher()).isEqualTo(Dispatcher.None)
    }

    @Test fun `plugin can override main dispatcher`() {
        val myDispatcher = IncrementingDispatcher()
        FormulaPlugins.setPlugin(TestPlugin(mainDispatcher = myDispatcher))
        assertThat(FormulaPlugins.mainThreadDispatcher()).isEqualTo(myDispatcher)
    }

    @Test fun `default plugin implementation does nothing on duplicate child key`() {
        val plugin = object : Plugin {}
        plugin.onDuplicateChildKey(
            parentType = Any::class.java,
            childFormulaType = Any::class.java,
            key = Unit,
        )
    }
}