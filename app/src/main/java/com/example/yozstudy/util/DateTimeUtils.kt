package com.example.yozstudy.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Helpers to work with dates stored as epoch milliseconds.
 * Only plain `java.util` and `java.text` classes are used, so the app works on every
 * supported Android version without extra setup.
 */
object DateTimeUtils {

    /** Midnight (00:00) of the day that contains [millis]. */
    fun startOfDay(millis: Long): Long = calendarOf(millis).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun isSameDay(first: Long, second: Long): Boolean {
        val a = calendarOf(first)
        val b = calendarOf(second)
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    /** Day of the week as a [Calendar] constant (Calendar.MONDAY ... Calendar.SUNDAY). */
    fun dayOfWeek(millis: Long): Int = calendarOf(millis).get(Calendar.DAY_OF_WEEK)

    fun hourOf(millis: Long): Int = calendarOf(millis).get(Calendar.HOUR_OF_DAY)

    fun minuteOf(millis: Long): Int = calendarOf(millis).get(Calendar.MINUTE)

    /** Same time of day as [millis], but on the given date. [month] is zero based (January = 0). */
    fun withDate(millis: Long, year: Int, month: Int, dayOfMonth: Int): Long =
        calendarOf(millis).apply { set(year, month, dayOfMonth) }.timeInMillis

    /** Same date as [millis], but at the given time. */
    fun withTime(millis: Long, hour: Int, minute: Int): Long = calendarOf(millis).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** The next full hour after [nowMillis]. For example 14:20 becomes 15:00. */
    fun nextFullHour(nowMillis: Long): Long = calendarOf(nowMillis).apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Days of the week in the order the day filter shows them, Monday first. */
    fun orderedDaysOfWeek(): List<Int> = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    )

    /** Date written in the language of the device, for example "Oct 16, 2026" or "16 oct 2026". */
    fun formatDate(millis: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis))

    /** Time written in the style of the device, for example "2:00 PM" or "14:00". */
    fun formatTime(millis: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(millis))

    /** Short name of a day in the language of the device, for example "Mon" or "lun". */
    fun formatDayShort(dayOfWeek: Int, locale: Locale = Locale.getDefault()): String {
        val calendar = Calendar.getInstance(locale).apply { set(Calendar.DAY_OF_WEEK, dayOfWeek) }
        return SimpleDateFormat("EEE", locale).format(calendar.time)
    }

    private fun calendarOf(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }
}
