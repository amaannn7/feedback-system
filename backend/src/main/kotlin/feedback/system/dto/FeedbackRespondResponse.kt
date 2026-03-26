package feedback.system.dto

data class FeedbackRespondResponse(
    val feedbackId: String,
    val status: String,
    val message: String
)
