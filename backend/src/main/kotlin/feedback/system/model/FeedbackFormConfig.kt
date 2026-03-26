package feedback.system.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("feedback_form_configs")
data class FeedbackFormConfig(
    @Id val id: String? = null,
    @Indexed(unique = true) val enterpriseId: String,
    val headerText: String,
    val headerDescription: String? = null,
    val footerText: String? = null,
    val ratingLabels: List<String>,
    val thankYouText: String,
    val invalidReplyText: String,
    val expiredReplyText: String,
    val skipForChannels: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
