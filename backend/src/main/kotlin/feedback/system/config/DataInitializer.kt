package feedback.system.config

import feedback.system.model.FeedbackFormConfig
import feedback.system.model.FeedbackRequest
import feedback.system.model.FeedbackStatus
import feedback.system.repository.FeedbackFormConfigRepository
import feedback.system.repository.FeedbackRequestRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class DataInitializer(
    private val formConfigRepo: FeedbackFormConfigRepository,
    private val feedbackRequestRepo: FeedbackRequestRepository
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (formConfigRepo.count() > 0) return

        // Enterprise feedback form configuration
        formConfigRepo.save(
            FeedbackFormConfig(
                enterpriseId = "demo-enterprise",
                headerText = "How was your experience?",
                headerDescription = "We'd love to hear about your recent chat interaction. Your feedback helps us improve!",
                footerText = "Your feedback is anonymous and appreciated.",
                ratingLabels = listOf("Very Poor", "Poor", "Average", "Good", "Excellent"),
                thankYouText = "Thank you for your feedback! We appreciate you taking the time to help us improve.",
                invalidReplyText = "Sorry, that's not a valid response. Please select a rating from 1 to 5.",
                expiredReplyText = "Sorry, this feedback link has expired. Feedback links are valid for 24 hours.",
                skipForChannels = emptyList()
            )
        )

        // Valid pending feedback request
        feedbackRequestRepo.save(
            FeedbackRequest(
                id = "feedback-valid",
                enterpriseId = "demo-enterprise",
                sessionId = "session-001",
                channel = "WEB_CHAT",
                status = FeedbackStatus.PENDING,
                expiresAt = Instant.now().plus(24, ChronoUnit.HOURS)
            )
        )

        // Expired feedback request
        feedbackRequestRepo.save(
            FeedbackRequest(
                id = "feedback-expired",
                enterpriseId = "demo-enterprise",
                sessionId = "session-002",
                channel = "WHATSAPP",
                status = FeedbackStatus.EXPIRED,
                expiresAt = Instant.now().minus(24, ChronoUnit.HOURS)
            )
        )

        // Already responded feedback request
        feedbackRequestRepo.save(
            FeedbackRequest(
                id = "feedback-used",
                enterpriseId = "demo-enterprise",
                sessionId = "session-003",
                channel = "INSTAGRAM",
                rating = 4,
                status = FeedbackStatus.RESPONDED,
                expiresAt = Instant.now().plus(12, ChronoUnit.HOURS),
                respondedAt = Instant.now().minus(1, ChronoUnit.HOURS)
            )
        )

        println("Demo data seeded successfully")
    }
}
