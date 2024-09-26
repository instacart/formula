package com.instacart.testutils.android

import com.instacart.formula.android.ActivityStoreContext
import com.instacart.testutils.android.TestActivityConfigurator.ActivityContextInitEvent
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class FormulaAndroidInteractor {
    private val activityContexts = mutableListOf<ActivityContextInitEvent>()
    private val relay = PublishRelay.create<Unit>()

    internal fun onActivityContextInitialized(event: ActivityContextInitEvent) {
        activityContexts.add(event)
        relay.accept(Unit)
    }

    fun <T : Any> selectEvents(
        selector: (ActivityStoreContext<*>) -> Observable<T>
    ): Observable<T> {
        return relay.switchMap {
            selector(lastActivityContext())
        }
    }

    private fun lastActivityContext(): ActivityStoreContext<*> {
        return activityContexts.last().activityContext
    }
}