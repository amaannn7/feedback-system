package feedback.system.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("feedback_requests")
data class FeedbackRequest(
    @Id val id: String? = null,
    val enterpriseId: String,
    val sessionId: String,
    val channel: String,
    val rating: Int? = null,
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val expiresAt: Instant,
    val respondedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)
