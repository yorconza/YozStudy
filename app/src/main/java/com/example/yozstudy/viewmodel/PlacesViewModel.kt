package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yozstudy.domain.model.PlaceStats
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.repository.StudyRepository
import com.example.yozstudy.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State of the map screen and the list of study places. */
data class PlacesUiState(
    val isLoading: Boolean = true,
    /** Places with their ratings, best rated first. Each one is a marker on the map. */
    val places: List<PlaceStats> = emptyList(),
    /** Place whose card is open at the bottom of the map. */
    val selectedPlace: PlaceStats? = null,
    /** The best rated place, or null if no place has been rated yet. */
    val bestPlace: PlaceStats? = null,
    /** Place waiting for the user to confirm its deletion. Not null means "show the dialog". */
    val pendingDeletePlace: StudyPlace? = null
)

/** ViewModel of the places: map markers, selection, create/edit and delete. */
class PlacesViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    private val controls = MutableStateFlow(PlacesControls())

    val uiState: StateFlow<PlacesUiState> = combine(
        repository.observePlaceStats(),
        controls
    ) { stats, current ->
        PlacesUiState(
            isLoading = false,
            places = stats,
            selectedPlace = stats.firstOrNull { it.place.id == current.selectedPlaceId },
            bestPlace = stats.filter { it.hasReviews }.maxByOrNull { it.overall },
            pendingDeletePlace = stats
                .firstOrNull { it.place.id == current.pendingDeleteId }
                ?.place
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_STOP_TIMEOUT_MS),
        initialValue = PlacesUiState()
    )

    /** Call with a place id when a marker is tapped, or with null to close the card. */
    fun onPlaceSelected(placeId: Long?) = controls.update { it.copy(selectedPlaceId = placeId) }

    /**
     * Creates the place (id 0) or updates it (id different from 0).
     *
     * @return false when the place was not saved because its name is empty.
     */
    fun onSavePlace(place: StudyPlace): Boolean {
        if (place.name.isBlank()) return false
        val cleanPlace = place.copy(name = place.name.trim(), address = place.address.trim())
        viewModelScope.launch {
            if (cleanPlace.id == 0L) repository.addPlace(cleanPlace) else repository.updatePlace(cleanPlace)
        }
        return true
    }

    fun onDeleteRequested(placeId: Long) = controls.update { it.copy(pendingDeleteId = placeId) }

    fun onDeleteDismissed() = controls.update { it.copy(pendingDeleteId = null) }

    fun onDeleteConfirmed() {
        val placeId = controls.value.pendingDeleteId ?: return
        controls.update {
            it.copy(
                pendingDeleteId = null,
                selectedPlaceId = if (it.selectedPlaceId == placeId) null else it.selectedPlaceId
            )
        }
        viewModelScope.launch { repository.deletePlace(placeId) }
    }
}

/** What the user chose on screen, before it is combined with the data. */
private data class PlacesControls(
    val selectedPlaceId: Long? = null,
    val pendingDeleteId: Long? = null
)
