package feedback.system.dto

data class FeedbackPageData(
    val feedbackId: String,
    val status: String,
    val form: FeedbackFormView?,
    val message: String?
)

data class FeedbackFormView(
    val headerText: String,
    val headerDescription: String?,
    val footerText: String?,
    val ratingLabels: List<String>
)
