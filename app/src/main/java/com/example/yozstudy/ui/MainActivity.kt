package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yozstudy.ServiceLocator
import com.example.yozstudy.databinding.ActivityMainBinding
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory

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
        setupListeners()

        // Carga inicial síncrona
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        // Refrescar al volver a primer plano por si se creó, editó o eliminó un bloque
        refreshUi()
    }

    private fun setupRecyclerView() {
        binding.rvStudyBlocks.layoutManager = LinearLayoutManager(this)
        // binding.rvStudyBlocks.adapter = yourAdapter
    }

    private fun refreshUi() {
        // Obtenemos los datos directamente del ViewModel de forma síncrona
        val blocks = viewModel.getFilteredBlocks()
        val progress = viewModel.getDailyProgress()

        // 1. Update the study blocks list
        updateStudyBlocksList(blocks)

        // 2. Update today's progress card using 'total' and 'done' from DailyProgress
        updateProgressCard(progress)
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
            val intent = Intent(this, StudyBlockFormActivity::class.java)
            startActivity(intent)
        }
    }
}