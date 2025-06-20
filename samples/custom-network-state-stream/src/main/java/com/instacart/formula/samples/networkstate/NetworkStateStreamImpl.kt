package com.instacart.formula.samples.networkstate

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import kotlinx.coroutines.CoroutineScope

class NetworkStateStreamImpl(private val application: Application) : NetworkStateStream {
    override fun start(scope: CoroutineScope, emitter: Action.Emitter<NetworkState>): Cancelable {
        // Broadcast receiver setup
        val action = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                emitter.onEvent(getNetworkState(application))
            }
        }

        // Register receiver
        application.registerReceiver(receiver, action, null, null);

        // Emit initial state
        emitter.onEvent(getNetworkState(application))

        return Cancelable {
            // Unregister
            application.unregisterReceiver(receiver)
        }
    }

    private fun getNetworkState(context: Context): NetworkState {
        val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isOnline = null != activeNetwork && activeNetwork.isConnected
        return NetworkState(isOnline)
    }
}
