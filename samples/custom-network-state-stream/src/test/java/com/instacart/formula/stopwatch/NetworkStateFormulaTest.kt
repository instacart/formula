package com.instacart.formula.stopwatch

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Cancelable
import com.instacart.formula.samples.networkstate.NetworkState
import com.instacart.formula.samples.networkstate.NetworkStateFormula
import com.instacart.formula.samples.networkstate.NetworkStateStream
import com.instacart.formula.test.test
import org.junit.Test

class NetworkStateFormulaTest {

    @Test fun offline() {
        formula(isOnline = false)
            .test()
            .input(Unit)
            .output {
                assertThat(status).isEqualTo("Network state: OFFLINE")
            }
    }

    @Test fun connected() {
        formula(isOnline = true)
            .test()
            .input(Unit)
            .output {
                assertThat(status).isEqualTo("Network state: CONNECTED")
            }
    }

    private fun formula(isOnline: Boolean) = NetworkStateFormula(networkStateStream(isOnline))

    private fun networkStateStream(isOnline: Boolean) = object : NetworkStateStream {
        override fun start(send: (NetworkState) -> Unit): Cancelable? {
            // Adding extra events to ensure that it handles multiple updates
            send(NetworkState(false))
            send(NetworkState(true))
            send(NetworkState(isOnline))
            return null
        }
    }
}
