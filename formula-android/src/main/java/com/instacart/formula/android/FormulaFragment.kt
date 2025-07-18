package com.instacart.formula.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.android.internal.getOrSetArguments
import java.lang.Exception

class FormulaFragment : Fragment() {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"
        internal const val ARG_FORMULA_ID = "formula:fragment:id"

        @JvmStatic
        fun newInstance(key: FragmentKey): FormulaFragment {
            return FormulaFragment().apply {
                getOrSetArguments().putParcelable(ARG_CONTRACT, key)
            }
        }
    }

    private val key: FragmentKey by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getParcelable<FragmentKey>(ARG_CONTRACT)!!
    }

    private val formulaFragmentId: FragmentId<*> by lazy {
        getFormulaFragmentId()
    }

    internal lateinit var fragmentStore: FragmentStore

    private val environment: FragmentEnvironment
        get() = fragmentStore.environment

    private val fragmentDelegate: FragmentEnvironment.FragmentDelegate
        get() = environment.fragmentDelegate

    private var featureView: FeatureView<Any>? = null
    private var output: Any? = null

    private val lifecycleCallback: FragmentLifecycleCallback?
        get() = featureView?.lifecycleCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewFactory = fragmentStore.getViewFactory(formulaFragmentId) ?: run {
            // No view factory, no view
            return null
        }
        val params = ViewFactory.Params(
            context = requireContext(),
            inflater = inflater,
            container = container,
        )

        val featureView = environment.fragmentDelegate.createView(
            fragmentId = formulaFragmentId,
            viewFactory = viewFactory,
            params = params,
        )
        this.featureView = featureView
        return featureView.view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryToSetState()

        lifecycleCallback?.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleCallback?.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        lifecycleCallback?.onStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleCallback?.onResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleCallback?.onPause()
    }

    override fun onStop() {
        super.onStop()
        lifecycleCallback?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lifecycleCallback?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        lifecycleCallback?.onLowMemory()
    }

    override fun onDestroyView() {
        lifecycleCallback?.onDestroyView()
        super.onDestroyView()
        featureView = null
    }

    fun setState(state: Any) {
        output = state
        tryToSetState()
    }

    fun currentState(): Any? {
        return output
    }

    fun getFragmentKey(): FragmentKey {
        return key
    }

    override fun toString(): String {
        return "${key.tag} -> $key"
    }

    private fun tryToSetState() {
        val output = output ?: return
        val view = featureView ?: return

        try {
            fragmentDelegate.setOutput(formulaFragmentId, output, view.setOutput)
        } catch (exception: Exception) {
            environment.onScreenError(key, exception)
        }
    }
}
