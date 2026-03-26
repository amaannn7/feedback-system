package feedback.system.service

import feedback.system.dto.*
import feedback.system.exception.FeedbackAlreadyRespondedException
import feedback.system.exception.FeedbackExpiredException
import feedback.system.exception.NotFoundException
import feedback.system.exception.ValidationException
import feedback.system.model.Channel
import feedback.system.model.FeedbackRequest
import feedback.system.model.FeedbackStatus
import feedback.system.repository.FeedbackFormConfigRepository
import feedback.system.repository.FeedbackRequestRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

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

    fun createFeedbackRequest(enterpriseId: String, request: CreateFeedbackRequestRequest): CreateFeedbackRequestResponse {
        feedbackFormConfigRepository.findByEnterpriseId(enterpriseId)
            ?: throw NotFoundException("No feedback form configured for enterprise: $enterpriseId")

        val errors = mutableMapOf<String, String>()
        if (request.sessionId.isNullOrBlank()) {
            errors["sessionId"] = "Session ID is required"
        }
        if (request.channel.isNullOrBlank()) {
            errors["channel"] = "Channel is required"
        } else {
            val validChannels = Channel.entries.map { it.name }.toSet()
            if (request.channel.trim().uppercase() !in validChannels) {
                errors["channel"] = "Invalid channel. Valid values: ${validChannels.joinToString()}"
            }
        }
        if (errors.isNotEmpty()) throw ValidationException(errors)

        val saved = feedbackRequestRepository.save(
            FeedbackRequest(
                enterpriseId = enterpriseId,
                sessionId = request.sessionId!!.trim(),
                channel = request.channel!!.trim().uppercase(),
                status = FeedbackStatus.PENDING,
                expiresAt = Instant.now().plus(24, ChronoUnit.HOURS)
            )
        )

        return CreateFeedbackRequestResponse(
            id = saved.id!!,
            enterpriseId = saved.enterpriseId,
            sessionId = saved.sessionId,
            channel = saved.channel,
            status = saved.status.name,
            expiresAt = saved.expiresAt,
            feedbackUrl = "/feedback/${saved.id}"
        )
    }

    fun getStats(enterpriseId: String): FeedbackStatsResponse {
        val pendingCount = feedbackRequestRepository.countByEnterpriseIdAndStatus(enterpriseId, FeedbackStatus.PENDING)
        val respondedCount = feedbackRequestRepository.countByEnterpriseIdAndStatus(enterpriseId, FeedbackStatus.RESPONDED)
        val expiredCount = feedbackRequestRepository.countByEnterpriseIdAndStatus(enterpriseId, FeedbackStatus.EXPIRED)
        val totalCount = pendingCount + respondedCount + expiredCount

        val respondedRequests = feedbackRequestRepository.findByEnterpriseIdAndStatus(enterpriseId, FeedbackStatus.RESPONDED)
        val ratings = respondedRequests.mapNotNull { it.rating }
        val averageRating = if (ratings.isNotEmpty()) ratings.average() else null
        val ratingDistribution = (1..5).associateWith { r -> ratings.count { it == r }.toLong() }

        return FeedbackStatsResponse(
            enterpriseId = enterpriseId,
            totalCount = totalCount,
            pendingCount = pendingCount,
            respondedCount = respondedCount,
            expiredCount = expiredCount,
            averageRating = averageRating,
            ratingDistribution = ratingDistribution
        )
    }

    fun listFeedbackRequests(enterpriseId: String): List<FeedbackRequestListItem> {
        return feedbackRequestRepository.findByEnterpriseId(enterpriseId)
            .sortedByDescending { it.createdAt }
            .map { toListItem(it) }
    }

    private fun toListItem(request: FeedbackRequest) = FeedbackRequestListItem(
        id = request.id!!,
        enterpriseId = request.enterpriseId,
        sessionId = request.sessionId,
        channel = request.channel,
        rating = request.rating,
        status = request.status.name,
        expiresAt = request.expiresAt,
        respondedAt = request.respondedAt,
        createdAt = request.createdAt
    )
}
