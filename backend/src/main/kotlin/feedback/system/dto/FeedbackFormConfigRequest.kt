package feedback.system.dto

data class FeedbackFormConfigRequest(
    val headerText: String?,
    val headerDescription: String?,
    val footerText: String?,
    val ratingLabels: List<String>?,
    val thankYouText: String?,
    val invalidReplyText: String?,
    val expiredReplyText: String?,
    val skipForChannels: List<String>?
)
