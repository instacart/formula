package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class ManyEmissionStreamTest {
    companion object {
        const val EMISSION_COUNT = 100000
    }

    @Test fun `all increment events go through`() {
        create()
            .test()
            .apply {
                assertThat(values()).containsExactly(EMISSION_COUNT)
            }
    }

    fun create() = run {
        val stream = RxStream.fromObservable {
            val values = 1..EMISSION_COUNT
            Observable.fromIterable(values)
        }

        TestUtils.create(0) { state, context ->
            Evaluation(
                updates = context.updates {
                    events(stream) {
                        transition(state + 1)
                    }
                },
                renderModel = state
            )
        }
    }
}
