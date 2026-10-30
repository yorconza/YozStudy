package com.example.yozstudy.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yozstudy.R
import com.example.yozstudy.ServiceLocator
import com.example.yozstudy.databinding.ActivityStudyBlocksBinding
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.SortOption
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.viewmodel.StudyBlockListUiState
import com.example.yozstudy.viewmodel.StudyBlockListViewModel
import com.example.yozstudy.viewmodel.StudyViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class StudyBlocksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBlocksBinding

    private val viewModel: StudyBlockListViewModel by viewModels {
        StudyViewModelFactory(ServiceLocator.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudyBlocksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSortSpinner()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.rvFilteredBlocks.layoutManager = LinearLayoutManager(this)
        // binding.rvFilteredBlocks.adapter = studyBlockAdapter
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: StudyBlockListUiState) {
        // 1. Update results count (using quantity strings plurals from strings.xml)
        val count = state.items.size
        binding.tvResultsCount.text = resources.getQuantityString(
            R.plurals.results_count,
            count,
            count
        )

        // 2. Populate subject chips if not already populated
        populateSubjectChips(state.subjects, state.filter.subjectId)

        // 3. Populate/update day chips
        populateDayChips(state.filter.dayOfWeek)

        // 4. Populate/update priority chips
        populatePriorityChips(state.filter.priority)

        // 5. Populate/update status chips
        populateStatusChips(state.filter.status)

        // 6. Submit updated list to the adapter
        updateFilteredList(state.items)
    }

    private fun populateSubjectChips(subjects: List<Subject>, selectedSubjectId: Long?) {
        binding.chipGroupSubjects.removeAllViews()

        // "All" chip
        val allChip = createChip(getString(R.string.filter_all_subjects), selectedSubjectId == null) {
            viewModel.onSubjectFilterChanged(null)
        }
        binding.chipGroupSubjects.addView(allChip)

        // Chip for each subject
        subjects.forEach { subject ->
            val isSelected = subject.id == selectedSubjectId
            val chip = createChip(subject.name, isSelected) {
                viewModel.onSubjectFilterChanged(subject.id)
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
            }
            binding.chipGroupDays.addView(chip)
        }
    }

    private fun populatePriorityChips(selectedPriority: Priority?) {
        // High, Medium, Low
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

    private fun updateFilteredList(items: List<StudyBlockWithDetails>) {
        // adapter.submitList(items)
    }
}