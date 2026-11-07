package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.yozstudy.R
import com.example.yozstudy.ServiceLocator
import com.example.yozstudy.databinding.ActivityStudyBlockDetailBinding
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.viewmodel.StudyBlockDetailViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory

class StudyBlockDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBlockDetailBinding

    private val viewModel: StudyBlockDetailViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBlockDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blockId = intent.getLongExtra(EXTRA_BLOCK_ID, -1L)
        if (blockId != -1L) {
            viewModel.initBlockId(blockId)
        }

        setupListeners()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, StudyBlockFormActivity::class.java)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.onDeleteConfirmed()
            finish()
        }

        binding.btnToggleStatus.setOnClickListener {
            val details = viewModel.getBlockDetails()
            val currentBlock = details?.block

            if (currentBlock?.isDone == true) {
                viewModel.onMarkPending()
            } else {
                viewModel.onMarkDone()
            }
            refreshUi()
        }
    }

    private fun refreshUi() {
        val details = viewModel.getBlockDetails()
        details?.let { updateUi(it) }
    }

    private fun updateUi(details: StudyBlockWithDetails) {
        val currentBlock = details.block
        binding.tvSubject.text = details.subject?.name ?: getString(R.string.filter_subject)
        binding.tvTopic.text = currentBlock.topic

        // Use startAtMillis to show the start time
        binding.tvDateTime.text = "Starts: ${currentBlock.startAtMillis}"
        binding.tvDuration.text = getString(R.string.duration_minutes, currentBlock.durationMinutes)

        // Priority using the custom drawables
        when (currentBlock.priority) {
            Priority.HIGH -> {
                binding.tvPriorityBadge.text = getString(R.string.priority_high)
                binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_high)
            }
            Priority.MEDIUM -> {
                binding.tvPriorityBadge.text = getString(R.string.priority_medium)
                binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_medium)
            }
            Priority.LOW -> {
                binding.tvPriorityBadge.text = getString(R.string.priority_low)
                binding.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_low)
            }
        }

        // Block status
        if (currentBlock.isDone) {
            binding.tvStatus.text = getString(R.string.status_done)
            binding.btnToggleStatus.text = getString(R.string.action_mark_pending)
        } else {
            binding.tvStatus.text = getString(R.string.status_pending)
            binding.btnToggleStatus.text = getString(R.string.action_mark_done)
        }

        binding.tvPlace.text = details.place?.name ?: getString(R.string.place_none)
    }

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
    }
}