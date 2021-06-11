package com.github.hwutimetable

import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun thatMatchesFirst(viewMatcher: Matcher<View>): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        private var isFirstViewFound = false
        private var matchedView: View? = null

        override fun matchesSafely(view: View?): Boolean {
            if (isFirstViewFound) {
                return matchedView == view
            }

            isFirstViewFound = viewMatcher.matches(view)
            if (isFirstViewFound) {
                matchedView = view
            }
            return isFirstViewFound
        }

        override fun describeTo(description: Description?) {
            description?.appendText("that matches first")
            viewMatcher.describeTo(description)
        }

    }
}