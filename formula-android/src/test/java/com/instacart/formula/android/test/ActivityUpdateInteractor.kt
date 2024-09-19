package com.instacart.formula.android.test

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.activity
import com.instacart.testutils.android.withFormulaAndroid
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

class ActivityUpdateInteractor(
    private val scenario: ActivityScenario<*>,
    private val updates: MutableList<Pair<Activity, String>>,
    private val updateRelay: PublishRelay<String>,
) {
    fun publish(update: String) {
        updateRelay.accept(update)
    }

    fun currentUpdates(): List<String> {
        return updates.filter { it.first == scenario.activity() }.map { it.second }
    }

    fun assertHasObservers(expected: Boolean) {
        assertThat(updateRelay.hasObservers()).isEqualTo(expected)
    }
}

fun runActivityUpdateTest(
    initialUpdates: Observable<String> = Observable.empty(),
    continuation: (ActivityScenario<TestFormulaActivity>, ActivityUpdateInteractor) -> Unit
) {
    val updates = mutableListOf<Pair<Activity, String>>()
    val updateRelay = PublishRelay.create<String>()
    withFormulaAndroid(
        configure = {
            activity<TestFormulaActivity> {
                ActivityStore(
                    streams = {
                        val updateEvents = initialUpdates.mergeWith(updateRelay)
                        update(updateEvents) { activity, state ->
                            updates.add(activity to state)
                        }
                    }
                )
            }
        }
    ) {
        val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
        val relay = ActivityUpdateInteractor(scenario, updates, updateRelay)
        continuation(scenario, relay)
    }
}


