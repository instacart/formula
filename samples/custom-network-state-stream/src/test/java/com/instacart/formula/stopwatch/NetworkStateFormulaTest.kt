package com.instacart.formula.stopwatch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.samples.networkstate.NetworkState
import com.instacart.formula.samples.networkstate.NetworkStateFormula
import com.instacart.formula.samples.networkstate.NetworkStateStream
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NetworkStateFormulaTest {

    @Test fun offline() = runTest {
        formula(isOnline = false)
            .test(this)
            .input(Unit)
            .output {
                assertThat(status).isEqualTo("Network state: OFFLINE")
            }
    }

    @Test fun connected() = runTest {
        formula(isOnline = true)
            .test(this)
            .input(Unit)
            .output {
                assertThat(status).isEqualTo("Network state: CONNECTED")
            }
    }

    private fun formula(isOnline: Boolean) = NetworkStateFormula(networkStateStream(isOnline))

    private fun networkStateStream(isOnline: Boolean) = object : NetworkStateStream {
        override fun start(
            scope: CoroutineScope,
            emitter: Action.Emitter<NetworkState>
        ): Cancelable? {
            // Adding extra events to ensure that it handles multiple updates
            emitter.onEvent(NetworkState(false))
            emitter.onEvent(NetworkState(true))
            emitter.onEvent(NetworkState(isOnline))
            return null
        }
    }
}
