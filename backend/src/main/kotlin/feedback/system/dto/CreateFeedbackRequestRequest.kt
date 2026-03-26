package feedback.system.dto

data class CreateFeedbackRequestRequest(
    val sessionId: String?,
    val channel: String?
)
