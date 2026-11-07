package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.PlaceReview
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.domain.repository.StudyRepository
import com.example.yozstudy.util.Constants

class StudyBlockDetailViewModel(
    private val repository: StudyRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private var blockId: Long = -1L
    private var showDeleteDialog: Boolean = false
    private var showRatePlaceDialog: Boolean = false
    private var isDeletedFlag: Boolean = false

    // ¡Este es el método que faltaba!
    fun initBlockId(id: Long) {
        this.blockId = id
    }

    fun getBlockDetails(): StudyBlockWithDetails? {
        if (blockId == -1L) return null
        val block = repository.getBlockSync(blockId) ?: return null
        val subjects = repository.getSubjectsSync()
        val places = repository.getPlacesSync()

        return StudyBlockWithDetails(
            block = block,
            subject = subjects.firstOrNull { it.id == block.subjectId },
            place = block.placeId?.let { id -> places.firstOrNull { it.id == id } }
        )
    }

    fun isDeleteDialogShown(): Boolean = showDeleteDialog
    fun isRatePlaceDialogShown(): Boolean = showRatePlaceDialog
    fun isDeleted(): Boolean = isDeletedFlag

    fun onMarkDone() {
        if (blockId == -1L) return
        val block = repository.getBlockSync(blockId) ?: return
        repository.setBlockStatusSync(blockId, BlockStatus.DONE)
        if (block.placeId != null && !block.isDone) {
            showRatePlaceDialog = true
        }
    }

    fun onMarkPending() {
        if (blockId != -1L) {
            repository.setBlockStatusSync(blockId, BlockStatus.PENDING)
        }
    }

    fun onDeleteRequested() {
        showDeleteDialog = true
    }

    fun onDeleteDismissed() {
        showDeleteDialog = false
    }

    fun onDeleteConfirmed() {
        if (blockId != -1L) {
            repository.deleteBlockSync(blockId)
        }
        showDeleteDialog = false
        isDeletedFlag = true
    }

    fun onRatePlaceDismissed() {
        showRatePlaceDialog = false
    }

    fun onRatePlaceSubmitted(silence: Int, wifi: Int, comfort: Int) {
        if (blockId == -1L) return
        val placeId = repository.getBlockSync(blockId)?.placeId
        showRatePlaceDialog = false
        if (placeId != null) {
            repository.addReviewSync(
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