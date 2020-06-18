package com.example.hwutimetable

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkUtilities(context: Context) {
    private val networkCapabilities: NetworkCapabilities?

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    }

    fun hasInternetConnection(): Boolean {
        val wifi = isWifiEnabled()
        val data = isMobileDataEnabled()
        return wifi || data
    }

    fun isWifiEnabled(): Boolean {
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    fun isMobileDataEnabled(): Boolean {
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
    }
}