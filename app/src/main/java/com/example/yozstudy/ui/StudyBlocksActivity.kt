package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yozstudy.R
import com.example.yozstudy.viewmodel.ServiceLocator
import com.example.yozstudy.ui.adapter.StudyBlockAdapter
import com.example.yozstudy.databinding.ActivityStudyBlocksBinding
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.SortOption
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory
import com.google.android.material.chip.Chip

class StudyBlocksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBlocksBinding
    private lateinit var blockAdapter: StudyBlockAdapter

    private val viewModel: StudyBlockListViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudyBlocksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSortSpinner()

        // Initial UI load
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        // Refresh every time the activity returns to the foreground in case of changes
        refreshUi()
    }

    private fun setupRecyclerView() {
        blockAdapter = StudyBlockAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, StudyBlockDetailActivity::class.java).apply {
                putExtra("EXTRA_BLOCK_ID", selectedItem.block.id)
            }
            startActivity(intent)
        }
        binding.rvFilteredBlocks.layoutManager = LinearLayoutManager(this)
        binding.rvFilteredBlocks.adapter = blockAdapter
    }

    private fun updateFilteredList(items: List<StudyBlockWithDetails>) {
        blockAdapter.updateData(items)
    }

    private fun setupSortSpinner() {
        val sortOptions = listOf(
            getString(R.string.sort_priority),
            getString(R.string.sort_date)
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spinnerSort.adapter = adapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSort = if (position == 0) SortOption.PRIORITY else SortOption.DATE
                viewModel.onSortChanged(selectedSort)
                refreshUi()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshUi() {
        val items = viewModel.getFilteredBlocks()
        val subjects = viewModel.getSubjects()
        val currentFilter = viewModel.getCurrentFilter()

        // 1. Update results count
        val count = items.size
        binding.tvResultsCount.text = resources.getQuantityString(
            R.plurals.results_count,
            count,
            count
        )

        // 2. Populate subject chips
        populateSubjectChips(subjects, currentFilter.subjectId)

        // 3. Populate day chips
        populateDayChips(currentFilter.dayOfWeek)

        // 4. Populate priority chips
        populatePriorityChips(currentFilter.priority)

        // 5. Populate status chips
        populateStatusChips(currentFilter.status)

        // 6. Submit updated list to the adapter
        updateFilteredList(items)
    }

    private fun populateSubjectChips(subjects: List<Subject>, selectedSubjectId: Long?) {
        binding.chipGroupSubjects.removeAllViews()

        // "All" chip
        val allChip = createChip(getString(R.string.filter_all_subjects), selectedSubjectId == null) {
            viewModel.onSubjectFilterChanged(null)
            refreshUi()
        }
        binding.chipGroupSubjects.addView(allChip)

        // Chip for each subject
        subjects.forEach { subject ->
            val isSelected = subject.id == selectedSubjectId
            val chip = createChip(subject.name, isSelected) {
                viewModel.onSubjectFilterChanged(subject.id)
                refreshUi()
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun populateDayChips(selectedDay: Int?) {
        binding.chipGroupDays.removeAllViews()

        // Days mapping (1 = Sun, 2 = Mon, ..., 7 = Sat according to java.util.Calendar)
        val daysMap = listOf(
            2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun"
        )

        daysMap.forEach { (dayConst, label) ->
            val isSelected = dayConst == selectedDay
            val chip = createChip(label, isSelected) {
                viewModel.onDayFilterChanged(if (isSelected) null else dayConst)
                refreshUi()
            }
            binding.chipGroupDays.addView(chip)
        }
    }

    private fun populatePriorityChips(selectedPriority: Priority?) {
        binding.chipGroupPriority.removeAllViews()

        val priorities = listOf(
            Priority.HIGH to getString(R.string.priority_high),
            Priority.MEDIUM to getString(R.string.priority_medium),
            Priority.LOW to getString(R.string.priority_low)
        )

        priorities.forEach { (priority, label) ->
            val isSelected = priority == selectedPriority
            val chip = createChip(label, isSelected) {
                viewModel.onPriorityFilterChanged(if (isSelected) null else priority)
                refreshUi()
            }
            binding.chipGroupPriority.addView(chip)
        }
    }

    private fun populateStatusChips(selectedStatus: BlockStatus?) {
        binding.chipGroupStatus.removeAllViews()

        val statuses = listOf(
            null to getString(R.string.filter_all_statuses),
            BlockStatus.PENDING to getString(R.string.status_pending),
            BlockStatus.DONE to getString(R.string.status_done)
        )

        statuses.forEach { (status, label) ->
            val isSelected = status == selectedStatus
            val chip = createChip(label, isSelected) {
                viewModel.onStatusFilterChanged(status)
                refreshUi()
            }
            binding.chipGroupStatus.addView(chip)
        }
    }

    private fun createChip(text: String, isChecked: Boolean, onClick: () -> Unit): Chip {
        return Chip(this).apply {
            this.text = text
            this.isCheckable = true
            this.isChecked = isChecked
            this.setOnClickListener { onClick() }
        }
    }
}