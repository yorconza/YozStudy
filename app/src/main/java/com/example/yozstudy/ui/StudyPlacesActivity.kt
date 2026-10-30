package com.example.yozstudy.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.yozstudy.R
import com.example.yozstudy.ServiceLocator
import com.example.yozstudy.databinding.ActivityStudyPlacesBinding
import com.example.yozstudy.domain.model.PlaceStats
import com.example.yozstudy.viewmodel.PlacesViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory

class StudyPlacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyPlacesBinding

    private val viewModel: PlacesViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun setupListeners() {
        // Setup search bar or map interaction listeners
    }

    private fun refreshUi() {
        // Obtenemos los datos de manera síncrona desde el ViewModel
        val statsList = viewModel.getPlaceStats()
        val selectedStats = viewModel.getSelectedPlace()

        updatePlacesUi(statsList, selectedStats)
    }

    private fun updatePlacesUi(statsList: List<PlaceStats>, selectedStats: PlaceStats?) {
        // 1. Update the saved places count in the header
        binding.tvPlacesCount.text = resources.getQuantityString(
            R.plurals.place_reviews_count,
            statsList.size,
            statsList.size
        )

        // 2. If there is a selected place, update the bottom card
        selectedStats?.let { stats ->
            val place = stats.place
            binding.tvPlaceNameCard.text = place.name

            // Update average score and corrected progress bars
            binding.tvScoreCard.text = getString(R.string.place_score, stats.reviewCount)

            binding.pbSilence.progress = (stats.averageSilence * 20).toInt()
            binding.pbWifi.progress = (stats.averageWifi * 20).toInt()
            binding.pbComfort.progress = (stats.averageComfort * 20).toInt()
        }
    }
}