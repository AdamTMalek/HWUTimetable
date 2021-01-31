package com.github.hwutimetable.extensions

import android.content.Context
import android.content.SharedPreferences

/**
 * Retrieve and hold the contents of the preferences, returning
 * a SharedPreferences through which you can retrieve and modify its
 * values.  Only one instance of the SharedPreferences object is returned
 * to any callers for the same name, meaning they will see each other's
 * edits as soon as they are made.
 *
 * <p>This method is thread-safe.
 *
 * <p>If the preferences directory does not already exist, it will be created when this method
 * is called.
 *
 * <p>If a preferences file by this name does not exist, it will be created when you retrieve an
 * editor ({@link SharedPreferences#edit()}) and then commit changes ({@link
 * SharedPreferences.Editor#commit()} or {@link SharedPreferences.Editor#apply()}).
 *
 * @param resId String resource id for the preference key.
 * @param mode Operating mode.
 *
 * @return The single {@link SharedPreferences} instance that can be used
 *         to retrieve and modify the preference values.
 *
 * @see #MODE_PRIVATE
 */
fun Context.getSharedPreferences(resId: Int, mode: Int): SharedPreferences {
    return getSharedPreferences(getString(resId), mode)
}
