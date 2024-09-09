package com.instacart.formula.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.internal.FormulaFragmentDelegate
import com.instacart.formula.android.internal.getFormulaFragmentId
import com.instacart.formula.android.internal.getOrSetArguments
import java.lang.Exception

class FormulaFragment : Fragment(), BaseFormulaFragment<Any> {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"
        internal const val ARG_FORMULA_ID = "formula:fragment:id"

        @JvmStatic
        fun newInstance(key: FragmentKey): FormulaFragment {
            val fragment = FormulaFragment()
            fragment.getOrSetArguments().apply {
                putParcelable(ARG_CONTRACT, key)
            }
            FormulaAndroid.fragmentEnvironment().fragmentDelegate.onNewInstance(
                fragmentId = fragment.formulaFragmentId
            )
            return fragment
        }
    }

    private val key: FragmentKey by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getParcelable<FragmentKey>(ARG_CONTRACT)!!
    }

    private val formulaFragmentId: FragmentId by lazy {
        getFormulaFragmentId()
    }

    private val environment: FragmentEnvironment
        get() = FormulaAndroid.fragmentEnvironment()

    private val fragmentDelegate: FragmentEnvironment.FragmentDelegate
        get() = environment.fragmentDelegate

    private var featureView: FeatureView<Any>? = null
    private var output: Any? = null

    private val lifecycleCallback: FragmentLifecycleCallback?
        get() = featureView?.lifecycleCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewFactory = FormulaFragmentDelegate.viewFactory(this) ?: run {
            // No view factory, no view
            return null
        }
        val featureView = viewFactory.create(inflater, container).apply {
            featureView = this
        }
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

    override fun setState(state: Any) {
        output = state
        tryToSetState()
    }

    override fun currentState(): Any? {
        return output
    }

    override fun getFragmentKey(): FragmentKey {
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
