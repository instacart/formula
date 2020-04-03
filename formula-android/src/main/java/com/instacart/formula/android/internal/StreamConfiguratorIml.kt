package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.StreamConfigurator
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

internal class StreamConfiguratorIml<out Activity : FragmentActivity>(
    private val context: ActivityStoreContextImpl<Activity>
) : StreamConfigurator<Activity> {

    override fun <State> update(
        state: Observable<State>,
        update: (Activity, State) -> Unit
    ): Disposable {
        // To keep activity & state in sync, we re-emit state on every activity change.
        val stateEmissions = Observable.combineLatest(
            state,
            context.activityStartedEvents(),
            BiFunction<State, Unit, State> { state, event ->
                state
            }
        )
        return stateEmissions.subscribe { state ->
            context.startedActivity()?.let {
                update(it, state)
            }
        }
    }
}