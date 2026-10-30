package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.PlaceReview
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.domain.repository.StudyRepository
import com.example.yozstudy.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State of the block detail screen. */
data class StudyBlockDetailUiState(
    val isLoading: Boolean = true,
    /** Null while loading, or when the block does not exist anymore. */
    val details: StudyBlockWithDetails? = null,
    val showDeleteDialog: Boolean = false,
    /** True after finishing a block that has a place: asks the student to rate that place. */
    val showRatePlaceDialog: Boolean = false,
    /** Becomes true once the block is deleted; the screen should close itself. */
    val isDeleted: Boolean = false
)

/** ViewModel of the detail screen of one study block. */
class StudyBlockDetailViewModel(
    private val repository: StudyRepository,
    private val blockId: Long,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val dialogs = MutableStateFlow(DialogFlags())

    val uiState: StateFlow<StudyBlockDetailUiState> = combine(
        repository.observeBlock(blockId),
        repository.observeSubjects(),
        repository.observePlaces(),
        dialogs
    ) { block, subjects, places, flags ->
        StudyBlockDetailUiState(
            isLoading = false,
            details = block?.let {
                StudyBlockWithDetails(
                    block = it,
                    subject = subjects.firstOrNull { subject -> subject.id == it.subjectId },
                    place = it.placeId?.let { id -> places.firstOrNull { place -> place.id == id } }
                )
            },
            showDeleteDialog = flags.showDelete,
            showRatePlaceDialog = flags.showRate,
            isDeleted = flags.isDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_STOP_TIMEOUT_MS),
        initialValue = StudyBlockDetailUiState()
    )

    /** Marks the block as done. If it has a place, the rate-place dialog is shown afterwards. */
    fun onMarkDone() {
        viewModelScope.launch {
            val block = repository.getBlock(blockId) ?: return@launch
            repository.setBlockStatus(blockId, BlockStatus.DONE)
            if (block.placeId != null && !block.isDone) {
                dialogs.update { it.copy(showRate = true) }
            }
        }
    }

    fun onMarkPending() {
        viewModelScope.launch { repository.setBlockStatus(blockId, BlockStatus.PENDING) }
    }

    // region Delete dialog

    fun onDeleteRequested() = dialogs.update { it.copy(showDelete = true) }

    fun onDeleteDismissed() = dialogs.update { it.copy(showDelete = false) }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            repository.deleteBlock(blockId)
            dialogs.update { it.copy(showDelete = false, isDeleted = true) }
        }
    }

    // endregion

    // region Rate-place dialog

    fun onRatePlaceDismissed() = dialogs.update { it.copy(showRate = false) }

    /** Saves the review of the place of this block. Every score goes from 1 to 5. */
    fun onRatePlaceSubmitted(silence: Int, wifi: Int, comfort: Int) {
        viewModelScope.launch {
            val placeId = repository.getBlock(blockId)?.placeId
            dialogs.update { it.copy(showRate = false) }
            if (placeId != null) {
                repository.addReview(
                    PlaceReview(
                        placeId = placeId,
                        blockId = blockId,
                        silence = silence.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE),
                        wifi = wifi.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE),
                        comfort = comfort.coerceIn(Constants.MIN_SCORE, Constants.MAX_SCORE),
                        createdAtMillis = clock()
                    )
                )
            }
        }
    }

    // endregion
}

/** Which dialogs are open, and whether the block was deleted. */
private data class DialogFlags(
    val showDelete: Boolean = false,
    val showRate: Boolean = false,
    val isDeleted: Boolean = false
)