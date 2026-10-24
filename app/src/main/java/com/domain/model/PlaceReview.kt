package com.domain.model

/** Place data   */
data class PlaceReview(
    val id: Long = 0L,
    val placeId: Long,
    val blockId: Long? = null,
    val silence: Int,
    val wifi: Int,
    val comfort: Int,
    val createdAtMillis: Long
) {
    /** Mean of the three scores. */
    val average: Float get() = (silence + wifi + comfort) / 3f
}

