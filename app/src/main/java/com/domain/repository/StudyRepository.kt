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
import kotlinx.coroutines.flow.Flow

/**
 * Single access point to the app data.
 *
 * Reads return a [Flow] so the screens update automatically when the data changes.
 * Writes are `suspend` functions so a persistent implementation (for example Room)
 * can replace [com.example.yozstudy.data.memory.MemoryDataRepository] without touching the ViewModels.
 */
interface StudyRepository {

    // region Study blocks

    /** Blocks that match [filter], sorted according to [sort]. */
    fun observeBlocks(
        filter: StudyBlockFilter = StudyBlockFilter(),
        sort: SortOption = SortOption.DATE
    ): Flow<List<StudyBlock>>

    /** Emits the block with the given id, or null if it does not exist (or was deleted). */
    fun observeBlock(blockId: Long): Flow<StudyBlock?>

    suspend fun getBlock(blockId: Long): StudyBlock?

    /** Saves a new block and returns the id assigned to it. */
    suspend fun addBlock(block: StudyBlock): Long

    suspend fun updateBlock(block: StudyBlock)

    suspend fun deleteBlock(blockId: Long)

    suspend fun setBlockStatus(blockId: Long, status: BlockStatus)

    /** Progress of the blocks planned for the day that contains [dayMillis]. */
    fun observeDailyProgress(dayMillis: Long): Flow<DailyProgress>

    // endregion

    // region Subjects

    fun observeSubjects(): Flow<List<Subject>>

    suspend fun addSubject(subject: Subject): Long

    // endregion

    // region Places

    fun observePlaces(): Flow<List<StudyPlace>>

    fun observePlace(placeId: Long): Flow<StudyPlace?>

    /** Places with their average ratings, best rated first. */
    fun observePlaceStats(): Flow<List<PlaceStats>>

    suspend fun addPlace(place: StudyPlace): Long

    suspend fun updatePlace(place: StudyPlace)

    /** Deletes the place and its reviews. Blocks that used it are kept, without a place. */
    suspend fun deletePlace(placeId: Long)

    // endregion

    // region Reviews

    fun observeReviews(placeId: Long): Flow<List<PlaceReview>>

    /** Saves a new review and returns the id assigned to it. Scores are limited to 1..5. */
    suspend fun addReview(review: PlaceReview): Long

    // endregion
}
