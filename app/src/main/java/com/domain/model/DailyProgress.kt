package com.domain.model


data class DailyProgress(
    val total: Int,
    val done: Int
) {
    /** Value from 0f to 1f, handy for a progress bar. */
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
}
