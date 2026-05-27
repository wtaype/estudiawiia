package com.estudiawii.app.core.streak

import android.content.Context
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val graceUsed: Int = 0,
    val graceTotal: Int = 3,
    val activeToday: Boolean = false,
    val lastOpenDate: String = "",
) {
    val graceLeft: Int get() = (graceTotal - graceUsed).coerceAtLeast(0)
}

object StreakStore {
    private const val PREFS = "EstudiaWii_streak"
    private const val KEY_CURRENT = "current"
    private const val KEY_BEST = "best"
    private const val KEY_GRACE_USED = "grace_used"
    private const val KEY_GRACE_MONTH = "grace_month"
    private const val KEY_LAST_OPEN = "last_open"

    fun touch(context: Context): StreakState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today).toString()
        val savedMonth = prefs.getString(KEY_GRACE_MONTH, thisMonth).orEmpty()
        val graceUsed = if (savedMonth == thisMonth) prefs.getInt(KEY_GRACE_USED, 0) else 0
        val lastRaw = prefs.getString(KEY_LAST_OPEN, "").orEmpty()
        val lastDate = runCatching { LocalDate.parse(lastRaw) }.getOrNull()
        val current = prefs.getInt(KEY_CURRENT, 0)
        val best = prefs.getInt(KEY_BEST, 0)

        if (lastDate == today) {
            return StreakState(current, best, graceUsed, activeToday = true, lastOpenDate = lastRaw)
        }

        val daysMissed = if (lastDate == null) 0 else ChronoUnit.DAYS.between(lastDate, today).toInt()
        val nextCurrent = when {
            lastDate == null -> current.coerceAtLeast(0) + 1
            daysMissed == 1 -> current + 1
            daysMissed == 2 && graceUsed < 3 -> current + 1
            else -> 1
        }
        val nextGraceUsed = if (lastDate != null && daysMissed == 2 && graceUsed < 3) graceUsed + 1 else graceUsed
        val nextBest = maxOf(best, nextCurrent)
        prefs.edit()
            .putInt(KEY_CURRENT, nextCurrent)
            .putInt(KEY_BEST, nextBest)
            .putInt(KEY_GRACE_USED, nextGraceUsed)
            .putString(KEY_GRACE_MONTH, thisMonth)
            .putString(KEY_LAST_OPEN, today.toString())
            .apply()
        return StreakState(nextCurrent, nextBest, nextGraceUsed, activeToday = true, lastOpenDate = today.toString())
    }

    fun load(context: Context): StreakState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        val month = YearMonth.now().toString()
        val savedMonth = prefs.getString(KEY_GRACE_MONTH, month).orEmpty()
        return StreakState(
            current = prefs.getInt(KEY_CURRENT, 0),
            best = prefs.getInt(KEY_BEST, 0),
            graceUsed = if (savedMonth == month) prefs.getInt(KEY_GRACE_USED, 0) else 0,
            activeToday = prefs.getString(KEY_LAST_OPEN, "") == today,
            lastOpenDate = prefs.getString(KEY_LAST_OPEN, "").orEmpty(),
        )
    }
}
