package com.example.yozstudy.data.memory

import com.domain.model.BlockStatus
import com.domain.model.DailyProgress
import com.domain.model.PlaceReview
import com.domain.model.PlaceStats
import com.domain.model.SortOption
import com.domain.model.StudyBlock
import com.domain.model.StudyBlockFilter
import com.domain.model.StudyPlace
import com.domain.model.Subject
import com.domain.repository.StudyRepository
import com.utils.Constants
import com.util.DateTimeUtils
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * [StudyRepository] that keeps everything in memory. Data is lost when the process dies,
 * which is enough for the first version of the app.
 */
class MemoryDataRepository(
    initialSubjects: List<Subject> = emptyList(),
    initialPlaces: List<StudyPlace> = emptyList(),
    initialBlocks: List<StudyBlock> = emptyList(),
    initialReviews: List<PlaceReview> = emptyList()
) : StudyRepository {

    private val subjects = MutableStateFlow(initialSubjects)
    private val places = MutableStateFlow(initialPlaces)
    private val blocks = MutableStateFlow(initialBlocks)
    private val reviews = MutableStateFlow(initialReviews)

    private val subjectIds = AtomicLong(initialSubjects.maxOfOrNull { it.id } ?: 0L)
    private val placeIds = AtomicLong(initialPlaces.maxOfOrNull { it.id } ?: 0L)
    private val blockIds = AtomicLong(initialBlocks.maxOfOrNull { it.id } ?: 0L)
    private val reviewIds = AtomicLong(initialReviews.maxOfOrNull { it.id } ?: 0L)

    // region Study blocks

    override fun observeBlocks(filter: StudyBlockFilter, sort: SortOption): Flow<List<StudyBlock>> =
        blocks.map { list ->
            list.filter { it.matches(filter) }.sortedWith(comparatorFor(sort))
        }

    override fun observeBlock(blockId: Long): Flow<StudyBlock?> =
        blocks.map { list -> list.firstOrNull { it.id == blockId } }.distinctUntilChanged()

    override suspend fun getBlock(blockId: Long): StudyBlock? =
        blocks.value.firstOrNull { it.id == blockId }

    override suspend fun addBlock(block: StudyBlock): Long {
        val id = blockIds.incrementAndGet()
        blocks.update { it + block.copy(id = id) }
        return id
    }

    override suspend fun updateBlock(block: StudyBlock) {
        blocks.update { list -> list.map { if (it.id == block.id) block else it } }
    }

    override suspend fun deleteBlock(blockId: Long) {
        blocks.update { list -> list.filterNot { it.id == blockId } }
        // Keep the review history, but it no longer points to a block that does not exist.
        reviews.update { list ->
            list.map { if (it.blockId == blockId) it.copy(blockId = null) else it }
        }
    }

    override suspend fun setBlockStatus(blockId: Long, status: BlockStatus) {
        blocks.update { list ->
            list.map { if (it.id == blockId) it.copy(status = status) else it }
        }
    }

    override fun observeDailyProgress(dayMillis: Long): Flow<DailyProgress> =
        blocks.map { list ->
            val ofTheDay = list.filter { DateTimeUtils.isSameDay(it.startAtMillis, dayMillis) }
            DailyProgress(total = ofTheDay.size, done = ofTheDay.count { it.isDone })
        }.distinctUntilChanged()

    // endregion

    // region Subjects

    override fun observeSubjects(): Flow<List<Subject>> =
        subjects.map { list -> list.sortedBy { it.name.lowercase() } }

    override suspend fun addSubject(subject: Subject): Long {
        val id = subjectIds.incrementAndGet()
        subjects.update { it + subject.copy(id = id) }
        return id
    }

    // endregion

    // region Places

    override fun observePlaces(): Flow<List<StudyPlace>> =
        places.map { list -> list.sortedBy { it.name.lowercase() } }

    override fun observePlace(placeId: Long): Flow<StudyPlace?> =
        places.map { list -> list.firstOrNull { it.id == placeId } }.distinctUntilChanged()

    override fun observePlaceStats(): Flow<List<PlaceStats>> =
        combine(places, reviews, blocks) { placeList, reviewList, blockList ->
            placeList.map { place ->
                val placeReviews = reviewList.filter { it.placeId == place.id }
                PlaceStats(
                    place = place,
                    reviewCount = placeReviews.size,
                    averageSilence = placeReviews.map { it.silence }.averageOrZero(),
                    averageWifi = placeReviews.map { it.wifi }.averageOrZero(),
                    averageComfort = placeReviews.map { it.comfort }.averageOrZero(),
                    completedBlocks = blockList.count { it.placeId == place.id && it.isDone }
                )
            }.sortedByDescending { it.overall }
        }

    override suspend fun addPlace(place: StudyPlace): Long {
        val id = placeIds.incrementAndGet()
        places.update { it + place.copy(id = id) }
        return id
    }

    override suspend fun updatePlace(place: StudyPlace) {
        places.update { list -> list.map { if (it.id == place.id) place else it } }
    }

    override suspend fun deletePlace(placeId: Long) {
        places.update { list -> list.filterNot { it.id == placeId } }
        blocks.update { list ->
            list.map { if (it.placeId == placeId) it.copy(placeId = null) else it }
        }
        reviews.update { list -> list.filterNot { it.placeId == placeId } }
    }

    // endregion

    // region Reviews

    override fun observeReviews(placeId: Long): Flow<List<PlaceReview>> =
        reviews.map { list ->
            list.filter { it.placeId == placeId }.sortedByDescending { it.createdAtMillis }
        }

    override suspend fun addReview(review: PlaceReview): Long {
        val id = reviewIds.incrementAndGet()
        val safeReview = review.copy(
            id = id,
            silence = review.silence.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE),
            wifi = review.wifi.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE),
            comfort = review.comfort.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE)
        )
        reviews.update { it + safeReview }
        return id
    }

    // endregion

    private fun StudyBlock.matches(filter: StudyBlockFilter): Boolean =
        (filter.subjectId == null || subjectId == filter.subjectId) &&
                (filter.dayOfWeek == null || DateTimeUtils.dayOfWeek(startAtMillis) == filter.dayOfWeek) &&
                (filter.priority == null || priority == filter.priority) &&
                (filter.status == null || status == filter.status)

    private fun comparatorFor(sort: SortOption): Comparator<StudyBlock> = when (sort) {
        SortOption.DATE -> compareBy { it.startAtMillis }
        SortOption.PRIORITY -> compareByDescending<StudyBlock> { it.priority.weight }
            .thenBy { it.startAtMillis }
    }

    private fun List<Int>.averageOrZero(): Float = if (isEmpty()) 0f else average().toFloat()

    companion object {
        /** Creates a repository filled with example data, so the screens are not empty at first. */
        fun withSampleData(now: Long = System.currentTimeMillis()): MemoryDataRepository =
            MemoryDataRepository(
                initialSubjects = SampleData.subjects(),
                initialPlaces = SampleData.places(),
                initialBlocks = SampleData.blocks(now),
                initialReviews = SampleData.reviews(now)
            )
    }
}
