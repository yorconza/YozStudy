package com.domain.model

/** A place where the student studies (library, cafe, home...). It is shown as a marker on the map. */
data class StudyPlace(
    val id: Long = 0L,
    val name: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double
)
