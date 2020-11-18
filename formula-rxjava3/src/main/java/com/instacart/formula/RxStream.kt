package com.instacart.formula

import com.instacart.formula.rxjava3.RxStream as RxStream3
import io.reactivex.rxjava3.core.Observable

/**
 * Formula [Stream] adapter to enable RxJava use.
 */
@Deprecated("Use RxStream in rxjava3 package")
object RxStream {
    /**
     * Creates a [Stream] from an [Observable] factory [create].
     *
     * ```
     * events(RxStream.fromObservable { locationManager.updates() }) { event ->
     *   transition()
     * }
     * ```
     */
    inline fun <Message> fromObservable(
        crossinline create: () -> Observable<Message>
    ): Stream<Message> {
        return RxStream3.fromObservable(create)
    }

    /**
     * Creates a [Stream] from an [Observable] factory [create].
     *
     * ```
     * events(RxStream.fromObservable(itemId) { repo.fetchItem(itemId) }) { event ->
     *   transition()
     * }
     * ```
     *
     * @param key Used to distinguish this [Stream] from other streams.
     */
    inline fun <Message> fromObservable(
        key: Any,
        crossinline create: () -> Observable<Message>
    ): Stream<Message> {
        return RxStream3.fromObservable(key, create)
    }
}
