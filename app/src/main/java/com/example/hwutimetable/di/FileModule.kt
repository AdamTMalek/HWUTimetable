package com.example.hwutimetable.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File

@InstallIn(ApplicationComponent::class)
@Module
object FileModule {
    @Provides
    fun provideDirectory(@ApplicationContext context: Context): File {
        return context.filesDir
    }
}