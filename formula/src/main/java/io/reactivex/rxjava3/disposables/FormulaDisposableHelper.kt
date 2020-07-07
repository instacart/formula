package io.reactivex.rxjava3.disposables

import java.util.Objects

/**
 * Used due to issue with [Disposable.fromRunnable], please ignore outside of the internal library.
 */
object FormulaDisposableHelper {

    /**
     * Identical to [Disposable.fromRunnable]
     */
    fun fromRunnable(run: Runnable): Disposable {
        Objects.requireNonNull<Runnable>(run, "run is null")
        return RunnableDisposable(run)
    }
}
