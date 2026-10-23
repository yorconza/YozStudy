package com.domain.model

/** A study block together with the subject and place it refers to, ready to be displayed. */
data class StudyBlockWithDetails(
    val block: StudyBlock,
    val subject: Subject?,
    val place: StudyPlace?
)
