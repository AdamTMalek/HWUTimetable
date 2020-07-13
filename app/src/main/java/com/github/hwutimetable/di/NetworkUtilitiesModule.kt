package com.github.hwutimetable.di

import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
abstract class NetworkUtilitiesModule {
    @Binds
    abstract fun bindNetworkUtilities(networkUtilities: NetworkUtilities): NetworkUtils
}