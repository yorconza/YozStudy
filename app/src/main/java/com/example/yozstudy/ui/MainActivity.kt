package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yozstudy.ServiceLocator
import com.example.yozstudy.databinding.ActivityMainBinding
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.viewmodel.StudyBlockFormUiState
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyBlockListUiState
import com.example.yozstudy.viewmodel.StudyViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ViewBinding reference for the activity_main.xml layout
    private lateinit var binding: ActivityMainBinding

    // ViewModel instance using the factory and passing the repository from ServiceLocator
    private val viewModel: StudyBlockListViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvStudyBlocks.layoutManager = LinearLayoutManager(this)
        // binding.rvStudyBlocks.adapter = yourAdapter
    }

    private fun setupObservers() {
        // Since the ViewModel uses StateFlow (uiState), we observe it safely with repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: StudyBlockListUiState) {
        // 1. Update the study blocks list
        updateStudyBlocksList(state.items)

        // 2. Update today's progress card using 'total' and 'done' from DailyProgress
        updateProgressCard(state.progress)
    }

    private fun updateStudyBlocksList(blocks: List<StudyBlockWithDetails>) {
        // Update your adapter with the list of blocks (e.g., adapter.submitList(blocks))
    }

    private fun updateProgressCard(progress: DailyProgress) {
        // We use 'progress.total' and 'progress.done' according to your DailyProgress class
        binding.pbDailyProgress.max = progress.total
        binding.pbDailyProgress.progress = progress.done

        binding.tvProgressCount.text = getString(
            com.example.yozstudy.R.string.progress_count,
            progress.done,
            progress.total
        )
    }

    private fun setupListeners() {
        // Floating action button to open the study block creation form
        binding.fabAddBlock.setOnClickListener {
            val intent = Intent(this, StudyBlockFormUiState::class.java)
            startActivity(intent)
        }
    }
}