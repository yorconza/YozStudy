package com.example.yozstudy.data.memory

import com.domain.model.BlockStatus
import com.domain.model.PlaceReview
import com.domain.model.Priority
import com.domain.model.StudyBlock
import com.domain.model.StudyPlace
import com.domain.Subject
import com.utils.DateTimeUtils

/**
 * Example data used to fill [MemoryDataRepository] the first time the app opens.
 * The coordinates are only placeholders; change them to places near you.
 */
internal object SampleData {

    fun subjects(): List<Subject> = listOf(
        Subject(id = 1, name = "Mathematics", colorArgb = 0xFF3F51B5.toInt()),
        Subject(id = 2, name = "English", colorArgb = 0xFF00897B.toInt()),
        Subject(id = 3, name = "Mobile Programming", colorArgb = 0xFF8E24AA.toInt()),
        Subject(id = 4, name = "Physics", colorArgb = 0xFFF4511E.toInt()),
        Subject(id = 5, name = "Databases", colorArgb = 0xFF1E88E5.toInt())
    )

    fun places(): List<StudyPlace> = listOf(
        StudyPlace(
            id = 1,
            name = "Central Library",
            address = "Main Avenue 100",
            latitude = 19.4326,
            longitude = -99.1332
        ),
        StudyPlace(
            id = 2,
            name = "Campus Cafe",
            address = "University Campus, Building B",
            latitude = 19.4340,
            longitude = -99.1400
        ),
        StudyPlace(
            id = 3,
            name = "Home Desk",
            address = "",
            latitude = 19.4300,
            longitude = -99.1280
        )
    )

    /** Five blocks planned for the day that contains [now]. */
    fun blocks(now: Long): List<StudyBlock> = listOf(
        StudyBlock(
            id = 1, subjectId = 1, topic = "Derivatives: exercises 1 to 10",
            startAtMillis = DateTimeUtils.withTime(now, 8, 0), durationMinutes = 60,
            priority = Priority.HIGH, status = BlockStatus.DONE, placeId = 1
        ),
        StudyBlock(
            id = 2, subjectId = 2, topic = "Unit 4 vocabulary",
            startAtMillis = DateTimeUtils.withTime(now, 10, 0), durationMinutes = 30,
            priority = Priority.LOW, status = BlockStatus.DONE, placeId = 3
        ),
        StudyBlock(
            id = 3, subjectId = 3, topic = "Room and ViewModel review",
            startAtMillis = DateTimeUtils.withTime(now, 14, 0), durationMinutes = 90,
            priority = Priority.HIGH, status = BlockStatus.PENDING, placeId = 1
        ),
        StudyBlock(
            id = 4, subjectId = 4, topic = "Kinematics summary",
            startAtMillis = DateTimeUtils.withTime(now, 16, 30), durationMinutes = 45,
            priority = Priority.MEDIUM, status = BlockStatus.PENDING, placeId = 2
        ),
        StudyBlock(
            id = 5, subjectId = 5, topic = "Normalization: assignment 3",
            startAtMillis = DateTimeUtils.withTime(now, 19, 0), durationMinutes = 60,
            priority = Priority.MEDIUM, status = BlockStatus.PENDING, placeId = 3
        )
    )

    fun reviews(now: Long): List<PlaceReview> = listOf(
        PlaceReview(id = 1, placeId = 1, blockId = 1, silence = 5, wifi = 4, comfort = 3, createdAtMillis = now),
        PlaceReview(id = 2, placeId = 3, blockId = 2, silence = 3, wifi = 5, comfort = 5, createdAtMillis = now),
        PlaceReview(id = 3, placeId = 1, blockId = null, silence = 4, wifi = 4, comfort = 4, createdAtMillis = now)
    )
}
