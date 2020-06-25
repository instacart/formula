package com.instacart.formula.samples.networkstate

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.instacart.formula.Cancelable

class NetworkStateStreamImpl(private val application: Application) : NetworkStateStream {

    override fun start(send: (NetworkState) -> Unit): Cancelable? {
        // Broadcast receiver setup
        val action = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                send(getNetworkState(application))
            }
        }

        // Register receiver
        application.registerReceiver(receiver, action, null, null);

        // Emit initial state
        send(getNetworkState(application))

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
