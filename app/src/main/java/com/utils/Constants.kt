package com.utils

object Constants {
    const val DEFAULT_DURATION_MINUTES = 60
    const val MIN_DURATION_MINUTES = 5
    const val MAX_DURATION_MINUTES = 480

    const val TOPIC_MAX_LENGTH = 80

    /** Range of the scores used to rate a place. */
    const val MIN_SCORE = 1
    const val MAX_SCORE = 5

    /** Time the screens may stay off before their state flows stop being collected. */
    const val STATE_STOP_TIMEOUT_MS = 5_000L
}