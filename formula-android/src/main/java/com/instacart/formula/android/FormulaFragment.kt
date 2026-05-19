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
import java.lang.Exception

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
        requireArguments().getParcelable<RouteKey>(ARG_CONTRACT)!!
    }

    private val formulaRouteId: RouteId<*> by lazy {
        getFormulaRouteId()
    }

    internal lateinit var navigationStore: NavigationStore

    private val environment: RouteEnvironment
        get() = navigationStore.environment

    private val routeDelegate: RouteEnvironment.RouteDelegate
        get() = environment.routeDelegate

    private var outputState: MutableState<Any?>? = null
    private var output: Any? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewFactory = navigationStore.getViewFactory(formulaRouteId) ?: return null
        val params = ViewFactory.Params(context = requireContext())
        val featureView = environment.routeDelegate.createView(
            routeId = formulaRouteId,
            viewFactory = viewFactory,
            params = params,
        )
        val state = mutableStateOf(featureView.initialModel)
        this.outputState = state
        return ComposeView(requireContext()).apply {
            // Based-on: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views#compose-in-fragments
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                state.value?.let { featureView.content(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryToSetState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        outputState = null
    }

    fun setState(state: Any) {
        output = state
        tryToSetState()
    }

    fun currentState(): Any? {
        return output
    }

    fun getRouteKey(): RouteKey {
        return key
    }

    override fun toString(): String {
        return "${key.tag} -> $key"
    }

    private fun tryToSetState() {
        val output = output ?: return
        val state = outputState ?: return
        try {
            routeDelegate.setOutput(formulaRouteId, output) { state.value = it }
        } catch (exception: Exception) {
            environment.onScreenError(key, exception)
        }
    }
}
