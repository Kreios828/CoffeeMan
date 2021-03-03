package kreios828.com.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import java.util.*

data class RatingPost(
    val uid: String = "", val username: String = "",
    val image: String = "",
    val caption: String = "",
    val comments: List<Comment> = emptyList(),
    val timestamp: Any = ServerValue.TIMESTAMP,
    val photo: String? = null,
    @Exclude val id: String = "",
    @Exclude val commentsCount: Int = 0
) { // Exclude чтобы Firebase не сохранял и не загружал этот атрибут
    // save -> Firebase puts Long value
    // get <- Long
    fun timestampDate(): Date = Date(timestamp as Long)
}