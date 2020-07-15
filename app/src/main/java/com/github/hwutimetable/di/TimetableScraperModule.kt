package com.github.hwutimetable.di

import com.github.hwutimetable.scraper.Scraper
import com.github.hwutimetable.scraper.TimetableScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class TimetableScraperModule {
    @Binds
    abstract fun bindTimetableScraper(scraper: Scraper): TimetableScraper
}