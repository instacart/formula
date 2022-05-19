package com.instacart.formula.samples.networkstate

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class NetworkStateFormula(
    private val networkStateStream: NetworkStateStream
) : Formula<Unit, NetworkStateFormula.State, NetworkStateRenderModel>() {

    data class State(val isOnline: Boolean = false)

    override fun initialState(input: Unit): State = State()

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<NetworkStateRenderModel> {
        val isConnected = if (state.isOnline) "CONNECTED" else "OFFLINE"
        return Evaluation(
            output = NetworkStateRenderModel(
                status = "Network state: $isConnected"
            ),
            actions = context.actions {
                events(networkStateStream) {
                    transition(state.copy(isOnline = it.isOnline))
                }
            }
        )
    }
}
