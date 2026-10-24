package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.BlockStatus
import com.domain.model.DailyProgress
import com.domain.model.Priority
import com.domain.model.SortOption
import com.domain.model.StudyBlock
import com.domain.model.StudyBlockFilter
import com.domain.model.StudyBlockWithDetails
import com.domain.model.Subject
import com.domain.repository.StudyRepository
import com.utils.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the "My routine" / filtered list screen needs to draw itself. */
data class StudyBlockListUiState(
    val isLoading: Boolean = true,
    val items: List<StudyBlockWithDetails> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val filter: StudyBlockFilter = StudyBlockFilter(),
    val sort: SortOption = SortOption.DATE,
    val progress: DailyProgress = DailyProgress(total = 0, done = 0),
    /** Block waiting for the user to confirm its deletion. Not null means "show the dialog". */
    val pendingDelete: StudyBlockWithDetails? = null
)

/** ViewModel of the study block list: filters, sorting, completing and deleting blocks. */
@OptIn(ExperimentalCoroutinesApi::class)
class StudyBlockListViewModel(
    private val repository: StudyRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val controls = MutableStateFlow(ListControls())

    private val blocks: Flow<List<StudyBlock>> = controls
        .map { it.filter to it.sort }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sort) -> repository.observeBlocks(filter, sort) }

    val uiState: StateFlow<StudyBlockListUiState> = combine(
        blocks,
        repository.observeSubjects(),
        repository.observePlaces(),
        repository.observeDailyProgress(clock()),
        controls
    ) { blockList, subjects, places, progress, current ->
        val subjectsById = subjects.associateBy { it.id }
        val placesById = places.associateBy { it.id }
        val items = blockList.map { block ->
            StudyBlockWithDetails(
                block = block,
                subject = subjectsById[block.subjectId],
                place = block.placeId?.let { id -> placesById[id] }
            )
        }
        StudyBlockListUiState(
            isLoading = false,
            items = items,
            subjects = subjects,
            filter = current.filter,
            sort = current.sort,
            progress = progress,
            pendingDelete = items.firstOrNull { it.block.id == current.pendingDeleteId }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_STOP_TIMEOUT_MS),
        initialValue = StudyBlockListUiState()
    )

    // region Filters and sorting

    fun onSubjectFilterChanged(subjectId: Long?) = updateFilter { it.copy(subjectId = subjectId) }

    /** [dayOfWeek] is a Calendar constant (Calendar.MONDAY ...) or null for every day. */
    fun onDayFilterChanged(dayOfWeek: Int?) = updateFilter { it.copy(dayOfWeek = dayOfWeek) }

    fun onPriorityFilterChanged(priority: Priority?) = updateFilter { it.copy(priority = priority) }

    fun onStatusFilterChanged(status: BlockStatus?) = updateFilter { it.copy(status = status) }

    fun onSortChanged(sort: SortOption) = controls.update { it.copy(sort = sort) }

    fun onClearFilters() = updateFilter { StudyBlockFilter() }

    // endregion

    // region Block actions

    /** Marks the block as done, or back as pending if it was already done. */
    fun onToggleDone(block: StudyBlock) {
        val newStatus = if (block.isDone) BlockStatus.PENDING else BlockStatus.DONE
        viewModelScope.launch { repository.setBlockStatus(block.id, newStatus) }
    }

    fun onDeleteRequested(blockId: Long) = controls.update { it.copy(pendingDeleteId = blockId) }

    fun onDeleteConfirmed() {
        val blockId = controls.value.pendingDeleteId ?: return
        controls.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch { repository.deleteBlock(blockId) }
    }

    fun onDeleteDismissed() = controls.update { it.copy(pendingDeleteId = null) }

    // endregion

    private fun updateFilter(transform: (StudyBlockFilter) -> StudyBlockFilter) {
        controls.update { it.copy(filter = transform(it.filter)) }
    }
}

/** What the user chose on screen, before it is combined with the data. */
private data class ListControls(
    val filter: StudyBlockFilter = StudyBlockFilter(),
    val sort: SortOption = SortOption.DATE,
    val pendingDeleteId: Long? = null
)
