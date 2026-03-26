package feedback.system.service

import feedback.system.dto.FeedbackFormView
import feedback.system.dto.FeedbackPageData
import feedback.system.dto.FeedbackRespondRequest
import feedback.system.dto.FeedbackRespondResponse
import feedback.system.exception.FeedbackAlreadyRespondedException
import feedback.system.exception.FeedbackExpiredException
import feedback.system.exception.NotFoundException
import feedback.system.exception.ValidationException
import feedback.system.model.FeedbackStatus
import feedback.system.repository.FeedbackFormConfigRepository
import feedback.system.repository.FeedbackRequestRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class FeedbackService(
    private val feedbackRequestRepository: FeedbackRequestRepository,
    private val feedbackFormConfigRepository: FeedbackFormConfigRepository
) {

    fun getFeedbackPageData(feedbackId: String): FeedbackPageData {
        val request = feedbackRequestRepository.findById(feedbackId)
            .orElseThrow { NotFoundException("Feedback request not found") }

        val config = feedbackFormConfigRepository.findByEnterpriseId(request.enterpriseId)

        // Check if expired (by time or status)
        if (request.status == FeedbackStatus.PENDING && request.expiresAt.isBefore(Instant.now())) {
            feedbackRequestRepository.save(request.copy(status = FeedbackStatus.EXPIRED))
            return FeedbackPageData(
                feedbackId = feedbackId,
                status = "EXPIRED",
                form = null,
                message = config?.expiredReplyText ?: "This feedback link has expired."
            )
        }

        if (request.status == FeedbackStatus.EXPIRED) {
            return FeedbackPageData(
                feedbackId = feedbackId,
                status = "EXPIRED",
                form = null,
                message = config?.expiredReplyText ?: "This feedback link has expired."
            )
        }

        if (request.status == FeedbackStatus.RESPONDED) {
            return FeedbackPageData(
                feedbackId = feedbackId,
                status = "RESPONDED",
                form = null,
                message = config?.thankYouText ?: "Thank you for your feedback!"
            )
        }

        // PENDING — show form
        if (config == null) {
            return FeedbackPageData(
                feedbackId = feedbackId,
                status = "ERROR",
                form = null,
                message = "Feedback form is not configured for this enterprise."
            )
        }

        return FeedbackPageData(
            feedbackId = feedbackId,
            status = "PENDING",
            form = FeedbackFormView(
                headerText = config.headerText,
                headerDescription = config.headerDescription,
                footerText = config.footerText,
                ratingLabels = config.ratingLabels
            ),
            message = null
        )
    }

    fun submitFeedback(feedbackId: String, request: FeedbackRespondRequest): FeedbackRespondResponse {
        if (request.rating == null || request.rating < 1 || request.rating > 5) {
            throw ValidationException(mapOf("rating" to "Rating must be between 1 and 5"))
        }

        val feedbackRequest = feedbackRequestRepository.findById(feedbackId)
            .orElseThrow { NotFoundException("Feedback request not found") }

        // Check expired
        if (feedbackRequest.status == FeedbackStatus.EXPIRED ||
            (feedbackRequest.status == FeedbackStatus.PENDING && feedbackRequest.expiresAt.isBefore(Instant.now()))
        ) {
            if (feedbackRequest.status == FeedbackStatus.PENDING) {
                feedbackRequestRepository.save(feedbackRequest.copy(status = FeedbackStatus.EXPIRED))
            }
            val config = feedbackFormConfigRepository.findByEnterpriseId(feedbackRequest.enterpriseId)
            throw FeedbackExpiredException(
                config?.expiredReplyText ?: "This feedback link has expired."
            )
        }

        // Check already responded
        if (feedbackRequest.status == FeedbackStatus.RESPONDED) {
            throw FeedbackAlreadyRespondedException("Feedback has already been submitted.")
        }

        // Save response
        feedbackRequestRepository.save(
            feedbackRequest.copy(
                rating = request.rating,
                status = FeedbackStatus.RESPONDED,
                respondedAt = Instant.now()
            )
        )

        val config = feedbackFormConfigRepository.findByEnterpriseId(feedbackRequest.enterpriseId)
        return FeedbackRespondResponse(
            feedbackId = feedbackId,
            status = "RESPONDED",
            message = config?.thankYouText ?: "Thank you for your feedback!"
        )
    }
}
