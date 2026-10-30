package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.domain.repository.StudyRepository
import com.example.yozstudy.util.Constants
import com.example.yozstudy.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State of the "New block" / "Edit block" form. */
data class StudyBlockFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val subjectId: Long? = null,
    val topic: String = "",
    val startAtMillis: Long = 0L,
    val durationMinutes: Int = Constants.DEFAULT_DURATION_MINUTES,
    val priority: Priority = Priority.MEDIUM,
    val placeId: Long? = null,
    val photoUri: String? = null,
    val subjects: List<Subject> = emptyList(),
    val places: List<StudyPlace> = emptyList(),
    val errors: Set<FormError> = emptySet(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    /** Becomes true once the block is saved; the screen should close itself. */
    val isSaved: Boolean = false
)

/**
 * ViewModel of the block form.
 *
 * @param blockId id of the block to edit, or null to create a new one.
 */
class StudyBlockFormViewModel(
    private val repository: StudyRepository,
    private val blockId: Long? = null,
    clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StudyBlockFormUiState(
            isEditing = blockId != null,
            isLoading = blockId != null,
            startAtMillis = DateTimeUtils.nextFullHour(clock())
        )
    )
    val uiState: StateFlow<StudyBlockFormUiState> = _uiState.asStateFlow()

    /** Status of the block being edited, so editing does not reset it to pending. */
    private var originalStatus: BlockStatus = BlockStatus.PENDING

    init {
        observeOptions()
        blockId?.let { loadBlock(it) }
    }

    // region Field changes

    fun onTopicChanged(value: String) = _uiState.update {
        it.copy(
            topic = value.take(Constants.TOPIC_MAX_LENGTH),
            errors = it.errors - FormError.TOPIC_EMPTY
        )
    }

    fun onSubjectSelected(subjectId: Long) = _uiState.update {
        it.copy(subjectId = subjectId, errors = it.errors - FormError.SUBJECT_MISSING)
    }

    fun onDurationChanged(minutes: Int) = _uiState.update {
        it.copy(durationMinutes = minutes, errors = it.errors - FormError.DURATION_INVALID)
    }

    fun onPriorityChanged(priority: Priority) = _uiState.update { it.copy(priority = priority) }

    /** Pass null for "no place". */
    fun onPlaceSelected(placeId: Long?) = _uiState.update { it.copy(placeId = placeId) }

    /** Pass the URI returned by the camera or the gallery, or null to remove the photo. */
    fun onPhotoSelected(photoUri: String?) = _uiState.update { it.copy(photoUri = photoUri) }

    // endregion

    // region Date and time dialogs

    fun onDatePickerRequested() = _uiState.update { it.copy(showDatePicker = true) }

    fun onDatePickerDismissed() = _uiState.update { it.copy(showDatePicker = false) }

    /** [month] is zero based (January = 0), like the Android date picker returns it. */
    fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) = _uiState.update {
        it.copy(
            startAtMillis = DateTimeUtils.withDate(it.startAtMillis, year, month, dayOfMonth),
            showDatePicker = false
        )
    }

    fun onTimePickerRequested() = _uiState.update { it.copy(showTimePicker = true) }

    fun onTimePickerDismissed() = _uiState.update { it.copy(showTimePicker = false) }

    fun onTimeSelected(hour: Int, minute: Int) = _uiState.update {
        it.copy(
            startAtMillis = DateTimeUtils.withTime(it.startAtMillis, hour, minute),
            showTimePicker = false
        )
    }

    // endregion

    fun onSaveClicked() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        val subjectId = state.subjectId ?: return
        val block = StudyBlock(
            id = blockId ?: 0L,
            subjectId = subjectId,
            topic = state.topic.trim(),
            startAtMillis = state.startAtMillis,
            durationMinutes = state.durationMinutes,
            priority = state.priority,
            status = originalStatus,
            placeId = state.placeId,
            photoUri = state.photoUri
        )
        viewModelScope.launch {
            if (blockId == null) repository.addBlock(block) else repository.updateBlock(block)
            _uiState.update { it.copy(isSaved = true, errors = emptySet()) }
        }
    }

    private fun validate(state: StudyBlockFormUiState): Set<FormError> = buildSet {
        if (state.topic.isBlank()) add(FormError.TOPIC_EMPTY)
        if (state.subjectId == null) add(FormError.SUBJECT_MISSING)
        if (state.durationMinutes !in Constants.MIN_DURATION_MINUTES..Constants.MAX_DURATION_MINUTES) {
            add(FormError.DURATION_INVALID)
        }
    }

    private fun observeOptions() {
        viewModelScope.launch {
            combine(repository.observeSubjects(), repository.observePlaces()) { subjects, places ->
                subjects to places
            }.collect { (subjects, places) ->
                _uiState.update { it.copy(subjects = subjects, places = places) }
            }
        }
    }

    private fun loadBlock(id: Long) {
        viewModelScope.launch {
            val block = repository.getBlock(id)
            if (block == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            originalStatus = block.status
            _uiState.update {
                it.copy(
                    isLoading = false,
                    subjectId = block.subjectId,
                    topic = block.topic,
                    startAtMillis = block.startAtMillis,
                    durationMinutes = block.durationMinutes,
                    priority = block.priority,
                    placeId = block.placeId,
                    photoUri = block.photoUri
                )
            }
        }
    }
}
