package com.example.yozstudy.data.repository

import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.PlaceReview
import com.example.yozstudy.domain.model.PlaceStats
import com.example.yozstudy.domain.model.SortOption
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyBlockFilter
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.domain.repository.StudyRepository

/**
 * In-memory implementation of StudyRepository providing synchronous CRUD operations.
 */
class MemoryDataRepository : StudyRepository {

    // Internal mock data lists to handle synchronous operations locally
    private val blocksList = mutableListOf<StudyBlock>()
    private val subjectsList = mutableListOf<Subject>()
    private val placesList = mutableListOf<StudyPlace>()
    private val reviewsList = mutableListOf<PlaceReview>()

    // region Study blocks

    override fun getBlocksSync(
        filter: StudyBlockFilter,
        sort: SortOption
    ): List<StudyBlock> {
        // Returns the list of blocks synchronously
        return blocksList.toList()
    }

    override fun getBlockSync(blockId: Long): StudyBlock? {
        // Finds and returns a single block by its ID
        return blocksList.find { it.id == blockId }
    }

    override fun addBlockSync(block: StudyBlock): Long {
        val newId = (blocksList.maxOfOrNull { it.id } ?: 0L) + 1L
        val newBlock = block.copy(id = newId)
        blocksList.add(newBlock)
        return newId
    }

    override fun updateBlockSync(block: StudyBlock) {
        val index = blocksList.indexOfFirst { it.id == block.id }
        if (index != -1) {
            blocksList[index] = block
        }
    }

    override fun deleteBlockSync(blockId: Long) {
        blocksList.removeIf { it.id == blockId }
    }

    override fun setBlockStatusSync(blockId: Long, status: BlockStatus) {
        val index = blocksList.indexOfFirst { it.id == blockId }
        if (index != -1) {
            val current = blocksList[index]
            // Update the block status depending on your model implementation
            // blocksList[index] = current.copy(status = status)
        }
    }

    override fun getDailyProgressSync(dayMillis: Long): DailyProgress {
        // Returns a basic daily progress object
        return DailyProgress(total = blocksList.size, done = 0)
    }

    // endregion

    // region Subjects

    override fun getSubjectsSync(): List<Subject> {
        return subjectsList.toList()
    }

    override fun addSubjectSync(subject: Subject): Long {
        val newId = (subjectsList.maxOfOrNull { it.id } ?: 0L) + 1L
        val newSubject = subject.copy(id = newId)
        subjectsList.add(newSubject)
        return newId
    }

    // endregion

    // region Places

    override fun getPlacesSync(): List<StudyPlace> {
        return placesList.toList()
    }

    override fun getPlaceSync(placeId: Long): StudyPlace? {
        return placesList.find { it.id == placeId }
    }

    override fun getPlaceStatsSync(): List<PlaceStats> {
        return emptyList()
    }

    override fun addPlaceSync(place: StudyPlace): Long {
        val newId = (placesList.maxOfOrNull { it.id } ?: 0L) + 1L
        val newPlace = place.copy(id = newId)
        placesList.add(newPlace)
        return newId
    }

    override fun updatePlaceSync(place: StudyPlace) {
        val index = placesList.indexOfFirst { it.id == place.id }
        if (index != -1) {
            placesList[index] = place
        }
    }

    override fun deletePlaceSync(placeId: Long) {
        placesList.removeIf { it.id == placeId }
    }

    // endregion

    // region Reviews

    override fun getReviewsSync(placeId: Long): List<PlaceReview> {
        return reviewsList.filter { it.placeId == placeId }
    }

    override fun addReviewSync(review: PlaceReview): Long {
        val newId = (reviewsList.maxOfOrNull { it.id } ?: 0L) + 1L
        val newReview = review.copy(id = newId)
        reviewsList.add(newReview)
        return newId
    }

    companion object {
        fun withSampleData(): MemoryDataRepository {
            return MemoryDataRepository()
        }
    }
    // endregion
}