package com.github.hwutimetable.di

import com.github.hwutimetable.scraper.CourseScraper
import com.github.hwutimetable.scraper.CourseTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class CourseScraperModule {
    @Binds
    abstract fun bindScraper(scraperModule: CourseScraper): CourseTimetableScraper
}