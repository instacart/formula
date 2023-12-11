package com.instacart.formula.android

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.android.internal.FormulaFragmentDelegate
import com.instacart.formula.android.internal.getFormulaFragmentId
import java.lang.Exception

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

    private var featureView: FeatureView<Any>? = null
    private var output: Any? = null

    private var initializedAtMillis: Long? = SystemClock.uptimeMillis()
    private var firstRender = true

    private val lifecycleCallback: FragmentLifecycleCallback?
        get() = featureView?.lifecycleCallbacks

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (initializedAtMillis == null) {
            initializedAtMillis = SystemClock.uptimeMillis()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firstRender = true

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
        initializedAtMillis = null

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

        val fragmentId = getFormulaFragmentId()
        val environment = FormulaFragmentDelegate.fragmentEnvironment()

        try {
            val start = SystemClock.uptimeMillis()
            view.setOutput(output)
            val end = SystemClock.uptimeMillis()


            environment.eventListener?.onRendered(
                fragmentId = fragmentId,
                durationInMillis = end - start,
            )

            if (firstRender) {
                firstRender = false
                environment.eventListener?.onFirstModelRendered(
                    fragmentId = fragmentId,
                    durationInMillis = end - (initializedAtMillis ?: SystemClock.uptimeMillis()),
                )
            }
        } catch (exception: Exception) {
            environment.onScreenError(key, exception)
        }
    }
}
