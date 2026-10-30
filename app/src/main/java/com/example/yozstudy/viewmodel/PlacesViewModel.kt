package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import com.example.yozstudy.domain.model.PlaceStats
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.repository.StudyRepository

/** ViewModel of the study places: search, stats and selection. */
class PlacesViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    private var selectedPlaceId: Long? = null

    // region Data retrieval

    fun getPlaceStats(): List<PlaceStats> {
        return repository.getPlaceStatsSync()
    }

    fun getSelectedPlace(): PlaceStats? {
        val statsList = repository.getPlaceStatsSync()
        return statsList.find { it.place.id == selectedPlaceId } ?: statsList.firstOrNull()
    }

    // endregion

    // region Actions

    fun onPlaceSelected(placeId: Long) {
        selectedPlaceId = placeId
    }

    fun onDeletePlace(placeId: Long) {
        repository.deletePlaceSync(placeId)
        if (selectedPlaceId == placeId) {
            selectedPlaceId = null
        }
    }

    // endregion
}