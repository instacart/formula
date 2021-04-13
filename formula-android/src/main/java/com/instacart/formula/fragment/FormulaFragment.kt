package com.instacart.formula.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.Cancelable
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory
import com.jakewharton.rxrelay3.BehaviorRelay

class FormulaFragment : Fragment(), BaseFormulaFragment<Any> {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"

        @JvmStatic
        fun newInstance(key: FragmentKey): FormulaFragment {
            return FormulaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONTRACT, key)
                }
            }
        }
    }

    private val key: FragmentKey by lazy(LazyThreadSafetyMode.NONE) {
        arguments!!.getParcelable<FragmentKey>(ARG_CONTRACT)!!
    }

    private lateinit var fragmentEnvironment: FragmentEnvironment
    internal var viewFactory: ViewFactory<Any>? = null
    private var featureView: FeatureView<Any>? = null
    private val stateRelay: BehaviorRelay<Any> = BehaviorRelay.create()
    private var cancelable: Cancelable? = null

    private var lifecycleCallback: FragmentLifecycleCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val uiFactory = viewFactory ?: throw IllegalStateException("Missing view factory: $key")
        return uiFactory
            .create(inflater, container)
            .apply { featureView = this }
            .view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureView = featureView!!
        val state = FeatureView.State(stateRelay, onError = {
            fragmentEnvironment.onScreenError(key, it)
        })
        cancelable = featureView.bind(state)
        this.lifecycleCallback = featureView.lifecycleCallbacks
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
        cancelable?.cancel()
        cancelable = null

        lifecycleCallback?.onDestroyView()
        lifecycleCallback = null
        super.onDestroyView()
        featureView = null
    }

    override fun setState(state: Any) {
        stateRelay.accept(state)
    }

    override fun currentState(): Any? {
        return stateRelay.value
    }

    override fun getFragmentKey(): FragmentKey {
        return key
    }

    internal fun setEnvironment(environment: FragmentEnvironment) {
        this.fragmentEnvironment = environment
    }

    override fun toString(): String {
        return "${key.tag} -> $key"
    }
}
