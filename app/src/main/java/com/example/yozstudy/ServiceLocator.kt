package com.example.yozstudy

import com.example.yozstudy.data.memory.MemoryDataRepository
import com.example.yozstudy.domain.repository.StudyRepository

/**
 * Gives every screen the same repository instance, so they all see the same data.
 *
 * To save data on the device later, only the line inside `lazy` has to change
 * (for example to a Room based repository); no ViewModel needs to be touched.
 */
object ServiceLocator {
    val repository: StudyRepository by lazy { MemoryDataRepository.withSampleData() }
}
