package com.domain.model

/**
 * Filters applied to the list of study blocks. A null field means "no filter".
 *
 * @property dayOfWeek a [java.util.Calendar] constant (Calendar.MONDAY ... Calendar.SUNDAY).
 */
data class StudyBlockFilter(
    val subjectId: Long? = null,
    val dayOfWeek: Int? = null,
    val priority: Priority? = null,
    val status: BlockStatus? = null
) {
    val isActive: Boolean
        get() = subjectId != null || dayOfWeek != null || priority != null || status != null
}
