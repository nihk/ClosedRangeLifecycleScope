package ca.nick.closedrangelifecyclescope

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Suppress("deprecation")
class NetworkState(private val context: Context) {

    private val connectivityFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    fun changes() = callbackFlow<Boolean> {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v("asdf", "Broadcast received")
                offer(isConnected())
            }
        }

        context.registerReceiver(broadcastReceiver, connectivityFilter)

        awaitClose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }
}