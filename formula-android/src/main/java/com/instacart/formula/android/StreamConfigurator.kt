package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Provides ability to configure RxJava streams that will survive configuration changes.
 */
interface StreamConfigurator<out Activity : FragmentActivity> {

    /**
     * Keeps activity in-sync with state observable updates. On activity configuration
     * changes, the last update is applied to new activity instance.
     *
     * @param state a state observable
     * @param update an update function
     */
    fun <State : Any> update(
        state: Observable<State>,
        update: (Activity, State) -> Unit
    ): Disposable
}
