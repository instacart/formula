package com.instacart.formula.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.Cancelable
import com.jakewharton.rxrelay3.BehaviorRelay

class FormulaFragment : Fragment(), BaseFormulaFragment<Any> {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"
        internal const val ARG_FORMULA_ID = "formula:fragment:id"

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
        requireArguments().getParcelable<FragmentKey>(ARG_CONTRACT)!!
    }

    internal lateinit var fragmentEnvironment: FragmentEnvironment
    internal lateinit var viewFactory: ViewFactory<Any>
    private var featureView: FeatureView<Any>? = null
    private val stateRelay: BehaviorRelay<Any> = BehaviorRelay.create()
    private var cancelable: Cancelable? = null

    private var lifecycleCallback: FragmentLifecycleCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val featureView = this.viewFactory.create(inflater, container).apply {
            featureView = this
        }
        return featureView.view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureView = featureView!!
        val state = FeatureView.State(
            observable = stateRelay,
            onError = {
                fragmentEnvironment.onScreenError(key, it)
            }
        )
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

    override fun toString(): String {
        return "${key.tag} -> $key"
    }
}
