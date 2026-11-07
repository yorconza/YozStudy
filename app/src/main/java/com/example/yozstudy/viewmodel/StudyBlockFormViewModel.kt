package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.domain.repository.StudyRepository
import com.example.yozstudy.util.Constants
import com.example.yozstudy.util.DateTimeUtils

/** State of the "New block" / "Edit block" form using direct synchronous properties. */
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
    val errors: Set<FormError> = emptySet(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    /** Becomes true once the block is saved; the screen should close itself. */
    val isSaved: Boolean = false
)

/**
 * ViewModel of the block form using synchronous logic.
 * @param blockId id of the block to edit, or null to create a new one.
 */
class StudyBlockFormViewModel(
    private val repository: StudyRepository,
    private val blockId: Long? = null,
    clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private var uiState = StudyBlockFormUiState(
        isEditing = blockId != null,
        isLoading = blockId != null,
        startAtMillis = DateTimeUtils.nextFullHour(clock())
    )

    /** Status of the block being edited, so editing does not reset it to pending. */
    private var originalStatus: BlockStatus = BlockStatus.PENDING

    init {
        blockId?.let { loadBlock(it) }
    }

    fun getUiState(): StudyBlockFormUiState = uiState

    // region Field changes

    fun onTopicChanged(value: String) {
        uiState = uiState.copy(
            topic = value.take(Constants.TOPIC_MAX_LENGTH),
            errors = uiState.errors - FormError.TOPIC_EMPTY
        )
    }

    fun onSubjectSelected(subjectId: Long) {
        uiState = uiState.copy(
            subjectId = subjectId,
            errors = uiState.errors - FormError.SUBJECT_MISSING
        )
    }

    fun onDurationChanged(minutes: Int) {
        uiState = uiState.copy(
            durationMinutes = minutes,
            errors = uiState.errors - FormError.DURATION_INVALID
        )
    }

    fun onPriorityChanged(priority: Priority) {
        uiState = uiState.copy(priority = priority)
    }

    /** Pass null for "no place". */
    fun onPlaceSelected(placeId: Long?) {
        uiState = uiState.copy(placeId = placeId)
    }

    /** Pass the URI returned by the camera or the gallery, or null to remove the photo. */
    fun onPhotoSelected(photoUri: String?) {
        uiState = uiState.copy(photoUri = photoUri)
    }

    // endregion

    // region Data retrieval for options

    fun getSubjects(): List<Subject> {
        return repository.getSubjectsSync()
    }

    fun getPlaces(): List<StudyPlace> {
        return repository.getPlacesSync()
    }

    // endregion

    // region Date and time dialogs

    fun onDatePickerRequested() {
        uiState = uiState.copy(showDatePicker = true)
    }

    fun onDatePickerDismissed() {
        uiState = uiState.copy(showDatePicker = false)
    }

    /** [month] is zero based (January = 0), like the Android date picker returns it. */
    fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
        uiState = uiState.copy(
            startAtMillis = DateTimeUtils.withDate(uiState.startAtMillis, year, month, dayOfMonth),
            showDatePicker = false
        )
    }

    fun onTimePickerRequested() {
        uiState = uiState.copy(showTimePicker = true)
    }

    fun onTimePickerDismissed() {
        uiState = uiState.copy(showTimePicker = false)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        uiState = uiState.copy(
            startAtMillis = DateTimeUtils.withTime(uiState.startAtMillis, hour, minute),
            showTimePicker = false
        )
    }

    // endregion

    fun onSaveClicked() {
        val errors = validate(uiState)
        if (errors.isNotEmpty()) {
            uiState = uiState.copy(errors = errors)
            return
        }
        val subjectId = uiState.subjectId ?: return
        val block = StudyBlock(
            id = blockId ?: 0L,
            subjectId = subjectId,
            topic = uiState.topic.trim(),
            startAtMillis = uiState.startAtMillis,
            durationMinutes = uiState.durationMinutes,
            priority = uiState.priority,
            status = originalStatus,
            placeId = uiState.placeId,
            photoUri = uiState.photoUri
        )

        if (blockId == null) {
            repository.addBlockSync(block)
        } else {
            repository.updateBlockSync(block)
        }
        uiState = uiState.copy(isSaved = true, errors = emptySet())
    }

    private fun validate(state: StudyBlockFormUiState): Set<FormError> = buildSet {
        if (state.topic.isBlank()) add(FormError.TOPIC_EMPTY)
        if (state.subjectId == null) add(FormError.SUBJECT_MISSING)
        if (state.durationMinutes !in Constants.MIN_DURATION_MINUTES..Constants.MAX_DURATION_MINUTES) {
            add(FormError.DURATION_INVALID)
        }
    }

    private fun loadBlock(id: Long) {
        val block = repository.getBlockSync(id)
        if (block == null) {
            uiState = uiState.copy(isLoading = false)
            return
        }
        originalStatus = block.status
        uiState = uiState.copy(
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