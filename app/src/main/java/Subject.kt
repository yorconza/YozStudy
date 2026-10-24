/**
 * A subject the student studies, for example "Mathematics".
 *
 * @property colorArgb color (ARGB) used to tint the cards of this subject.
 */
data class Subject(
    val id: Long = 0L,
    val name: String,
    val colorArgb: Int
)
