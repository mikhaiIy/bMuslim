package com.bmuslim.app.ui.almanac

import android.content.Context

private const val AlmanacLearningPreferences = "almanac_interaction_learning"
private const val QuietGripLearnedKey = "quiet_grip_learned"

internal class AlmanacWheelLearningStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        AlmanacLearningPreferences,
        Context.MODE_PRIVATE,
    )

    fun isQuietGripLearned(): Boolean = preferences.getBoolean(QuietGripLearnedKey, false)

    fun markQuietGripLearned() {
        preferences.edit().putBoolean(QuietGripLearnedKey, true).apply()
    }

    fun resetQuietGripForDebug(): Boolean = preferences.edit().remove(QuietGripLearnedKey).commit()
}

/** Internal engineering/test reset path; model-key changes intentionally never call this. */
internal fun resetQuietGripLearningForDebug(context: Context): Boolean =
    AlmanacWheelLearningStore(context).resetQuietGripForDebug()
