package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yozstudy.databinding.ActivityMainBinding
import com.example.yozstudy.domain.model.DailyProgress
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.ui.adapter.StudyBlockAdapter
import com.example.yozstudy.viewmodel.ServiceLocator
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var blockAdapter: StudyBlockAdapter

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
        blockAdapter = StudyBlockAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, StudyBlockDetailActivity::class.java).apply {
                putExtra("EXTRA_BLOCK_ID", selectedItem.block.id)
            }
            startActivity(intent)
        }
        binding.rvStudyBlocks.layoutManager = LinearLayoutManager(this)
        binding.rvStudyBlocks.adapter = blockAdapter
    }

    private fun refreshUi() {
        val blocks = viewModel.getFilteredBlocks()
        val progress = viewModel.getDailyProgress()

        // 1. Actualizar la lista de tarjetas
        updateStudyBlocksList(blocks)

        // 2. Actualizar tarjeta de progreso diario
        updateProgressCard(progress)
    }

    private fun updateStudyBlocksList(blocks: List<StudyBlockWithDetails>) {
        blockAdapter.updateData(blocks)
    }

    private fun updateProgressCard(progress: DailyProgress) {
        binding.pbDailyProgress.max = if (progress.total > 0) progress.total else 1
        binding.pbDailyProgress.progress = progress.done

        binding.tvProgressCount.text = getString(
            com.example.yozstudy.R.string.progress_count,
            progress.done,
            progress.total
        )
    }

    private fun setupListeners() {
        binding.btnOpenForm.setOnClickListener {
            startActivity(Intent(this, StudyBlocksFormActivity::class.java))
        }

        binding.btnOpenPlaces.setOnClickListener {
            startActivity(Intent(this, StudyPlacesActivity::class.java))
        }
    }
}