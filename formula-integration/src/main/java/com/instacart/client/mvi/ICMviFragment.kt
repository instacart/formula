package com.instacart.client.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.instacart.client.core.unsafeLazy
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable
import timber.log.Timber

class ICMviFragment<RenderModel> : Fragment(), ICBaseMviFragment<RenderModel> {
    companion object {
        private const val ARG_CONTRACT = "mvi fragment contract"

        @JvmStatic fun <State> newInstance(contract: ICMviFragmentContract<State>): ICMviFragment<State> {
            return ICMviFragment<State>().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONTRACT, contract)
                }
            }
        }
    }

    private val contract: ICMviFragmentContract<RenderModel> by unsafeLazy {
        arguments!!.getParcelable<ICMviFragmentContract<RenderModel>>(ARG_CONTRACT)
    }

    // State relay + disposable
    private val stateRelay: BehaviorRelay<RenderModel> = BehaviorRelay.create()
    private var disposable: Disposable? = null

    private var lifecycleCallback: ICFragmentLifecycleCallback? = null
    private var mviView: ICMviView<RenderModel>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(contract.layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val component = contract.createComponent(view)
        this.mviView = component.mviView
        disposable = stateRelay
            .doOnNext {
                Timber.d("render / ${this@ICMviFragment}")
            }
            .subscribe(component.mviView.renderer::render, Timber::e)

        this.lifecycleCallback = component.lifecycleCallbacks
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
        mviView = null
    }

    override fun setState(state: RenderModel) {
        Timber.d("setState / $this")
        stateRelay.accept(state)
    }

    override fun currentState(): RenderModel? {
        return stateRelay.value
    }

    override fun getMviContract(): ICMviFragmentContract<RenderModel> {
        return contract
    }

    fun mviView(): ICMviView<RenderModel>? = mviView

    override fun toString(): String {
        return "${contract.tag} -> $contract"
    }
}
