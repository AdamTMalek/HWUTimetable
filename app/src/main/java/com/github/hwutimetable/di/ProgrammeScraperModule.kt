package com.github.hwutimetable.di

import com.github.hwutimetable.scraper.ProgrammeScraper
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ProgrammeScraperModule {
    @Binds
    abstract fun bindScraper(scraper: ProgrammeScraper): ProgrammeTimetableScraper
}