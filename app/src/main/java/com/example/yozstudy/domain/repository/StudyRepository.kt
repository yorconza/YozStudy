package com.example.yozstudy.domain.repository

import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.PlaceReview
import com.example.yozstudy.domain.model.PlaceStats
import com.example.yozstudy.domain.model.SortOption
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyBlockFilter
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.model.Subject

/** Single access point to the app data using direct synchronous methods. */
interface StudyRepository {

    // region Study blocks

    /** Blocks that match [filter], sorted according to [sort]. */
    fun getBlocksSync(
        filter: StudyBlockFilter = StudyBlockFilter(),
        sort: SortOption = SortOption.DATE
    ): List<StudyBlock>

    fun getBlockSync(blockId: Long): StudyBlock?

    /** Saves a new block and returns the id assigned to it. */
    fun addBlockSync(block: StudyBlock): Long

    fun updateBlockSync(block: StudyBlock)

    fun deleteBlockSync(blockId: Long)

    fun setBlockStatusSync(blockId: Long, status: BlockStatus)

    /** Progress of the blocks planned for the day that contains [dayMillis]. */
    fun getDailyProgressSync(dayMillis: Long): DailyProgress

    // endregion

    // region Subjects

    fun getSubjectsSync(): List<Subject>

    fun addSubjectSync(subject: Subject): Long

    // endregion

    // region Places

    fun getPlacesSync(): List<StudyPlace>

    fun getPlaceSync(placeId: Long): StudyPlace?

    fun getPlaceStatsSync(): List<PlaceStats>

    fun addPlaceSync(place: StudyPlace): Long

    fun updatePlaceSync(place: StudyPlace)

    fun deletePlaceSync(placeId: Long)

    // endregion

    // region Reviews

    fun getReviewsSync(placeId: Long): List<PlaceReview>

    fun addReviewSync(review: PlaceReview): Long

    // endregion
}