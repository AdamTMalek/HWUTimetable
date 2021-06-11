package com.github.hwutimetable.di

import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkUtilitiesModule {
    @Binds
    abstract fun bindNetworkUtilities(networkUtilities: NetworkUtilities): NetworkUtils
}