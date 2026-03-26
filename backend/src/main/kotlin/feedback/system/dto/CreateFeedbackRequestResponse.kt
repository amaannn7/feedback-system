package feedback.system.dto

import java.time.Instant

data class CreateFeedbackRequestResponse(
    val id: String,
    val enterpriseId: String,
    val sessionId: String,
    val channel: String,
    val status: String,
    val expiresAt: Instant,
    val feedbackUrl: String
)
