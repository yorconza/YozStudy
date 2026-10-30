package com.example.yozstudy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yozstudy.domain.repository.StudyRepository

/**
 * Creates the ViewModels of the app, which need a repository in their constructor.
 */
class StudyViewModelFactory(
    private val repository: StudyRepository,
    private val blockId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(StudyBlockListViewModel::class.java) ->
            StudyBlockListViewModel(repository) as T

        modelClass.isAssignableFrom(StudyBlockFormViewModel::class.java) ->
            StudyBlockFormViewModel(repository, blockId) as T

        modelClass.isAssignableFrom(StudyBlockDetailViewModel::class.java) -> {
            val viewModel = StudyBlockDetailViewModel(repository)
            if (blockId != null && blockId != -1L) {
                viewModel.initBlockId(blockId)
            }
            viewModel as T
        }

        modelClass.isAssignableFrom(PlacesViewModel::class.java) ->
            PlacesViewModel(repository) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}