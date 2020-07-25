package com.github.hwutimetable.di

import com.github.hwutimetable.CurrentDateProvider
import com.github.hwutimetable.CurrentLocalDateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class CurrentDateProviderModule {
    @Binds
    abstract fun bindProvider(provider: CurrentLocalDateProvider): CurrentDateProvider
}