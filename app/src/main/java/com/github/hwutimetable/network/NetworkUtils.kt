package com.github.hwutimetable.network

interface NetworkUtils {
    /**
     * Returns true if the device has any connection to the Internet
     */
    fun hasInternetConnection(): Boolean

    /**
     * Returns true if the device has WiFi enabled
     */
    fun isWifiEnabled(): Boolean

    /**
     * Returns true if the device has mobile data enabled
     */
    fun isMobileDataEnabled(): Boolean
}