package com.instacart.formula.android.persistence

import android.os.Parcelable

interface PersistedState<State : Parcelable> {

    /**
     * Gets the current [State] value.
     */
    fun current(): State?

    /**
     * Sets the latest [State] value that will be persisted across process death.
     */
    fun save(state: State?)
}
