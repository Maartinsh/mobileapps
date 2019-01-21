package com.experiment.scope.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(
    context: Context
) {
    companion object {
        const val PREFERENCE_NAME = "com.experiment.scope"
        const val PREFERENCE_PREVIOUS_USERS_SYNC_TIME = "com.experiment.scope.PREFERENCE_PREVIOUS_USERS_SYNC_TIME"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getPreviousUsersSyncTime(): Long {
        return sharedPreferences.getLong(PREFERENCE_PREVIOUS_USERS_SYNC_TIME, 0)
    }

    fun setPreviousUsersSyncTime(syncTimeMillis: Long) {
        sharedPreferences
            .edit()
            .putLong(PREFERENCE_PREVIOUS_USERS_SYNC_TIME, syncTimeMillis)
            .apply()
    }

}