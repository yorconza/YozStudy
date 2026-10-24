package com.domain.model

/**
 * A planned study session.
 *
 * @property startAtMillis start date and time, in epoch milliseconds.
 * @property durationMinutes planned duration of the session.
 * @property placeId place where the student studies, or null when not chosen.
 * @property photoUri URI of an attached photo (notes, whiteboard), or null.
 */
data class StudyBlock(
    val id: Long = 0L,
    val subjectId: Long,
    val topic: String,
    val startAtMillis: Long,
    val durationMinutes: Int,
    val priority: Priority = Priority.MEDIUM,
    val status: BlockStatus = BlockStatus.PENDING,
    val placeId: Long? = null,
    val photoUri: String? = null
) {
    val isDone: Boolean get() = status == BlockStatus.DONE
}
