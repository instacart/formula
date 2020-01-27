package com.instacart.formula.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.formula.RenderView
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

class FormulaFragment<RenderModel> : Fragment(), BaseFormulaFragment<RenderModel> {
    companion object {
        private const val ARG_CONTRACT = "formula fragment contract"

        @JvmStatic fun <State> newInstance(contract: FragmentContract<State>): FormulaFragment<State> {
            return FormulaFragment<State>().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONTRACT, contract)
                }
            }
        }
    }

    private val contract: FragmentContract<RenderModel> by lazy(LazyThreadSafetyMode.NONE) {
        arguments!!.getParcelable<FragmentContract<RenderModel>>(ARG_CONTRACT)!!
    }

    // State relay + disposable
    private val stateRelay: BehaviorRelay<RenderModel> = BehaviorRelay.create()
    private var disposable: Disposable? = null

    private var lifecycleCallback: FragmentLifecycleCallback? = null
    private var renderView: RenderView<RenderModel>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(contract.layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val component = contract.createComponent(view)
        this.renderView = component.renderView
        disposable = stateRelay
            .doOnNext {
                // Timber.d("render / ${this@FormulaFragment}")
            }
            .subscribe(component.renderView.renderer::render)

        this.lifecycleCallback = component.lifecycleCallbacks
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
        disposable?.dispose()
        disposable = null

        lifecycleCallback?.onDestroyView()
        lifecycleCallback = null
        super.onDestroyView()
        renderView = null
    }

    override fun setState(state: RenderModel) {
        // Timber.d("setState / $this")
        stateRelay.accept(state)
    }

    override fun currentState(): RenderModel? {
        return stateRelay.value
    }

    override fun getFragmentContract(): FragmentContract<RenderModel> {
        return contract
    }

    fun renderView(): RenderView<RenderModel>? = renderView

    override fun toString(): String {
        return "${contract.tag} -> $contract"
    }
}
