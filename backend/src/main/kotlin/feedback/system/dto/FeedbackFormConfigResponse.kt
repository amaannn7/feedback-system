package feedback.system.dto

data class FeedbackFormConfigResponse(
    val enterpriseId: String,
    val headerText: String,
    val headerDescription: String?,
    val footerText: String?,
    val ratingLabels: List<String>,
    val thankYouText: String,
    val invalidReplyText: String,
    val expiredReplyText: String,
    val skipForChannels: List<String>
)
