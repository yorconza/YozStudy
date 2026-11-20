package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yozstudy.R
import com.example.yozstudy.ui.adapter.StudyBlockAdapter
import com.example.yozstudy.viewmodel.ServiceLocator
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory

class StudyBlocksActivity : AppCompatActivity() {

    private lateinit var rvStudyBlocks: RecyclerView
    private lateinit var blockAdapter: StudyBlockAdapter

    private val viewModel: StudyBlockListViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_blocks)

        setupRecyclerView()
        loadBlocks()
    }

    override fun onResume() {
        super.onResume()
        loadBlocks()
    }

    private fun setupRecyclerView() {
        rvStudyBlocks = findViewById(R.id.rvStudyBlocks)
        blockAdapter = StudyBlockAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, StudyBlockDetailActivity::class.java).apply {
                putExtra("EXTRA_BLOCK_ID", selectedItem.block.id)
            }
            startActivity(intent)
        }
        rvStudyBlocks.layoutManager = LinearLayoutManager(this)
        rvStudyBlocks.adapter = blockAdapter
    }

    private fun loadBlocks() {
        val items = viewModel.getFilteredBlocks()
        blockAdapter.updateData(items)
    }
}