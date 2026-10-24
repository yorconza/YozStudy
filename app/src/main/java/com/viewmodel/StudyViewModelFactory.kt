package com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.domain.repository.StudyRepository

/**
 * Creates the ViewModels of the app, which need a repository in their constructor.
 *
 * Example inside an Activity or a Fragment:
 * ```
 * private val viewModel: StudyBlockListViewModel by viewModels {
 *     StudyViewModelFactory(ServiceLocator.repository)
 * }
 * ```
 *
 * @param blockId needed by [StudyBlockDetailViewModel], optional for [StudyBlockFormViewModel]
 *                (null creates a new block, an id edits that block).
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

        modelClass.isAssignableFrom(StudyBlockDetailViewModel::class.java) ->
            StudyBlockDetailViewModel(
                repository,
                requireNotNull(blockId) { "StudyBlockDetailViewModel needs a blockId" }
            ) as T

        modelClass.isAssignableFrom(PlacesViewModel::class.java) ->
            PlacesViewModel(repository) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
