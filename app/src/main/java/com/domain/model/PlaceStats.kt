package com.domain.model

//Informacion del lugar escogido
data class PlaceStats(
    val place: StudyPlace,
    val reviewCount: Int,
    val averageSilence: Float,
    val averageWifi: Float,
    val averageComfort: Float,
    val completedBlocks: Int
) {
    val hasReviews: Boolean get() = reviewCount > 0

    /** Overall score from 0 to 5. It is 0 when the place has no reviews yet. */
    val overall: Float
        get() = if (hasReviews) (averageSilence + averageWifi + averageComfort) / 3f else 0f
}
