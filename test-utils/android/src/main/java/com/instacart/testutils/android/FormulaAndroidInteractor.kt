package com.instacart.testutils.android

import com.instacart.formula.android.ActivityStoreContext
import com.instacart.testutils.android.TestActivityConfigurator.ActivityContextInitEvent
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

class FormulaAndroidInteractor {
    private val activityContexts = mutableListOf<ActivityContextInitEvent>()
    private val relay = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val rxRelay = PublishRelay.create<Unit>()

    internal fun onActivityContextInitialized(event: ActivityContextInitEvent) {
        activityContexts.add(event)
        relay.tryEmit(Unit)
        rxRelay.accept(Unit)
    }

    fun <T : Any> selectEvents(
        selector: (ActivityStoreContext<*>) -> Observable<T>
    ): Observable<T> {
        return rxRelay.switchMap {
            selector(lastActivityContext())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T: Any> collectEvents(
        scope: TestScope,
        collector: suspend (ActivityStoreContext<*>) -> Flow<T>,
    ) : List<T> {
        val events = mutableListOf<T>()
        scope.backgroundScope.launch(
            start = CoroutineStart.UNDISPATCHED,
            context = Dispatchers.Unconfined,
        ) {
            relay
                .flatMapLatest { collector(lastActivityContext()) }
                .collect { events.add(it) }
        }
        return events
    }

    private fun lastActivityContext(): ActivityStoreContext<*> {
        return activityContexts.last().activityContext
    }
}