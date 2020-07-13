package com.github.hwutimetable.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkUtilities @Inject constructor(@ApplicationContext context: Context) : NetworkUtils {
    /**
     * Any class that wants to receive notifications about Internet connection
     * loss or gain via [ConnectivityCallback] class, has to implement this
     * interface.
     */
    interface ConnectivityCallbackReceiver {
        /**
         * Executed when the device has gained connection to the Internet
         */
        fun onConnectionAvailable()

        /**
         * Executed when the device has lost connection to the Internet
         */
        fun onConnectionLost()
    }

    /**
     * The [ConnectivityManager] uses the [ConnectivityCallbackReceiver] to
     * inform any class that implements that interface when the device
     * looses connection to the Internet or gains it back.
     * It will use [ConnectivityManager.NetworkCallback] to monitor
     * WiFi and Cellular networks to determine if the connection to the
     * Internet exists or not.
     *
     * Unlike [ConnectivityManager.NetworkCallback] on its own, this class
     * does not care about a single network loss if there are different
     * networks available.
     *
     * It is important to unregister the internal network callback when exiting from
     * an activity. Use onDestroy() and call [cleanup].
     *
     * Note that the callback functions [ConnectivityCallbackReceiver.onConnectionLost]
     * and [ConnectivityCallbackReceiver.onConnectionAvailable] will be invoked on
     * a different thread than the UI.
     */
    class ConnectivityCallback(context: Context) {
        private val connectivityManager: ConnectivityManager = context
            .applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        private val availableNetworks = mutableListOf<Network>()
        private val receivers = mutableListOf<ConnectivityCallbackReceiver>()

        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                availableNetworks.add(network)

                if (availableNetworks.size == 1)  // The device just gained internet access
                    receivers.forEach { it.onConnectionAvailable() }
            }

            override fun onLost(network: Network) {
                availableNetworks.remove(network)

                if (availableNetworks.isEmpty())
                    receivers.forEach { it.onConnectionLost() }
            }
        }

        init {
            addAllExistingNetworks()
            registerNetworkCallback()
        }

        fun registerCallbackReceiver(receiver: ConnectivityCallbackReceiver) {
            receivers.add(receiver)
        }

        private fun addAllExistingNetworks() {
            // Add all WiFi or Cellular networks to the availableNetworks list
            availableNetworks.addAll(
                connectivityManager.allNetworks.filter { network ->
                    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@filter false
                    return@filter capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            )
        }

        private fun registerNetworkCallback() {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }

        /**
         * Unregisters the internal [networkCallback]. Always call this function
         * in Activity's onDestroy() function
         */
        fun cleanup() {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private val networkCapabilities: NetworkCapabilities?

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    }

    override fun hasInternetConnection(): Boolean {
        val wifi = isWifiEnabled()
        val data = isMobileDataEnabled()
        return wifi || data
    }

    override fun isWifiEnabled(): Boolean {
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    override fun isMobileDataEnabled(): Boolean {
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
    }
}