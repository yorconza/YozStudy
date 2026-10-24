package com.domain.model

/**Priority levels, there are 3 levels here*/
enum class Priority(val weight: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3)
}
