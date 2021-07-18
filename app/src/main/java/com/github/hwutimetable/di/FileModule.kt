package com.github.hwutimetable.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File

@InstallIn(SingletonComponent::class)
@Module
object FileModule {
    @Provides
    fun provideDirectory(@ApplicationContext context: Context): File {
        return context.filesDir
    }
}