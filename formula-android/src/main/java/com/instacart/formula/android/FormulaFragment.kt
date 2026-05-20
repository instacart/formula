package com.instacart.formula.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.instacart.formula.android.internal.getOrSetArguments

class FormulaFragment : Fragment() {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"
        internal const val ARG_FORMULA_ID = "formula:fragment:id"

        @JvmStatic
        fun newInstance(key: RouteKey): FormulaFragment {
            return FormulaFragment().apply {
                getOrSetArguments().putParcelable(ARG_CONTRACT, key)
            }
        }
    }

    private val key: RouteKey by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getParcelable(ARG_CONTRACT)!!
    }

    private val formulaRouteId: RouteId<*> by lazy(LazyThreadSafetyMode.NONE) {
        getFormulaRouteId()
    }

    internal lateinit var navigationStore: NavigationStore

    private val environment: RouteEnvironment
        get() = navigationStore.environment

    private val routeDelegate: RouteEnvironment.RouteDelegate
        get() = environment.routeDelegate

    private val outputState: MutableState<Any?> = mutableStateOf(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewFactory = navigationStore.getViewFactory(formulaRouteId) ?: return null
        val initial: Any? = when (viewFactory) {
            is ComposeViewFactory -> viewFactory.initialModel()
        }
        return ComposeView(requireContext()).apply {
            // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val output = outputState.value ?: initial
                if (output != null) {
                    routeDelegate.Content(formulaRouteId, viewFactory, output)
                }
            }
        }
    }

    fun setState(state: Any) {
        try {
            routeDelegate.setOutput(formulaRouteId, state) { outputState.value = it }
        } catch (exception: Exception) {
            environment.onScreenError(key, exception)
        }
    }

    fun currentState(): Any? = outputState.value

    fun getRouteKey(): RouteKey {
        return key
    }

    override fun toString(): String {
        return "${key.tag} -> $key"
    }
}
