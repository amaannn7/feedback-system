package feedback.system.dto

import java.time.Instant

data class FeedbackRequestListItem(
    val id: String,
    val enterpriseId: String,
    val sessionId: String,
    val channel: String,
    val rating: Int?,
    val status: String,
    val expiresAt: Instant,
    val respondedAt: Instant?,
    val createdAt: Instant
)
