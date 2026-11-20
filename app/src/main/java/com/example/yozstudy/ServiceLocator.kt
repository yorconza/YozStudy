package com.example.yozstudy.viewmodel


import com.example.yozstudy.data.repository.MemoryDataRepository
import com.example.yozstudy.domain.repository.StudyRepository

/**
 * Service locator that provides a single shared instance of StudyRepository.
 */


object ServiceLocator {
    val repository: StudyRepository by lazy { MemoryDataRepository.withSampleData() }
}