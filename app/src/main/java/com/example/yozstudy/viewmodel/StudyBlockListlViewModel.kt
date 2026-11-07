package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.SortOption
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyBlockFilter
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.domain.repository.StudyRepository

/** ViewModel of the study block list: filters, sorting, completing and deleting blocks. */
class StudyBlockListViewModel(
    private val repository: StudyRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private var currentFilter = StudyBlockFilter()
    private var currentSort = SortOption.DATE
    private var pendingDeleteId: Long? = null

    // region Filters and sorting

    fun getCurrentFilter(): StudyBlockFilter = currentFilter

    fun onSubjectFilterChanged(subjectId: Long?) {
        currentFilter = currentFilter.copy(subjectId = subjectId)
    }

    /** [dayOfWeek] is a Calendar constant (Calendar.MONDAY ...) or null for every day. */
    fun onDayFilterChanged(dayOfWeek: Int?) {
        currentFilter = currentFilter.copy(dayOfWeek = dayOfWeek)
    }

    fun onPriorityFilterChanged(priority: Priority?) {
        currentFilter = currentFilter.copy(priority = priority)
    }

    fun onStatusFilterChanged(status: BlockStatus?) {
        currentFilter = currentFilter.copy(status = status)
    }

    fun onSortChanged(sort: SortOption) {
        currentSort = sort
    }

    fun onClearFilters() {
        currentFilter = StudyBlockFilter()
    }

    // endregion

    // region Data retrieval

    fun getFilteredBlocks(): List<StudyBlockWithDetails> {
        val blocks = repository.getBlocksSync(currentFilter, currentSort)
        val subjects = repository.getSubjectsSync()
        val places = repository.getPlacesSync()

        val subjectsById = subjects.associateBy { it.id }
        val placesById = places.associateBy { it.id }

        return blocks.map { block ->
            StudyBlockWithDetails(
                block = block,
                subject = subjectsById[block.subjectId],
                place = block.placeId?.let { id -> placesById[id] }
            )
        }
    }

    fun getSubjects(): List<Subject> {
        return repository.getSubjectsSync()
    }

    fun getDailyProgress(): DailyProgress {
        return repository.getDailyProgressSync(clock())
    }

    // endregion

    // region Block actions

    /** Marks the block as done, or back as pending if it was already done. */
    fun onToggleDone(block: StudyBlock) {
        val newStatus = if (block.isDone) BlockStatus.PENDING else BlockStatus.DONE
        repository.setBlockStatusSync(block.id, newStatus)
    }

    fun onDeleteRequested(blockId: Long) {
        pendingDeleteId = blockId
    }

    fun onDeleteConfirmed() {
        val blockId = pendingDeleteId ?: return
        pendingDeleteId = null
        repository.deleteBlockSync(blockId)
    }

    fun onDeleteDismissed() {
        pendingDeleteId = null
    }

    // endregion
}