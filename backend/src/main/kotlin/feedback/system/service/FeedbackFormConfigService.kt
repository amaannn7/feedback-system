package feedback.system.service

import feedback.system.dto.FeedbackFormConfigRequest
import feedback.system.dto.FeedbackFormConfigResponse
import feedback.system.exception.ValidationException
import feedback.system.model.Channel
import feedback.system.model.FeedbackFormConfig
import feedback.system.repository.FeedbackFormConfigRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class FeedbackFormConfigService(
    private val repository: FeedbackFormConfigRepository
) {

    fun getConfig(enterpriseId: String): FeedbackFormConfigResponse? {
        val config = repository.findByEnterpriseId(enterpriseId) ?: return null
        return toResponse(config)
    }

    fun saveConfig(enterpriseId: String, request: FeedbackFormConfigRequest): FeedbackFormConfigResponse {
        val errors = validate(request)
        if (errors.isNotEmpty()) throw ValidationException(errors)

        val existing = repository.findByEnterpriseId(enterpriseId)
        val config = FeedbackFormConfig(
            id = existing?.id,
            enterpriseId = enterpriseId,
            headerText = request.headerText!!.trim(),
            headerDescription = request.headerDescription?.trim()?.ifBlank { null },
            footerText = request.footerText?.trim()?.ifBlank { null },
            ratingLabels = request.ratingLabels!!.map { it.trim() },
            thankYouText = request.thankYouText!!.trim(),
            invalidReplyText = request.invalidReplyText!!.trim(),
            expiredReplyText = request.expiredReplyText!!.trim(),
            skipForChannels = request.skipForChannels
                ?.map { it.trim().uppercase() }
                ?.distinct()
                ?: emptyList(),
            createdAt = existing?.createdAt ?: Instant.now(),
            updatedAt = Instant.now()
        )
        return toResponse(repository.save(config))
    }

    fun validate(request: FeedbackFormConfigRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.headerText.isNullOrBlank()) {
            errors["headerText"] = "Header text is required"
        } else if (request.headerText.trim().length > 200) {
            errors["headerText"] = "Header text must be at most 200 characters"
        }

        if (request.headerDescription != null && request.headerDescription.isNotBlank()
            && request.headerDescription.trim().length > 500
        ) {
            errors["headerDescription"] = "Header description must be at most 500 characters"
        }

        if (request.footerText != null && request.footerText.isNotBlank()
            && request.footerText.trim().length > 200
        ) {
            errors["footerText"] = "Footer text must be at most 200 characters"
        }

        if (request.ratingLabels == null || request.ratingLabels.isEmpty()) {
            errors["ratingLabels"] = "Rating labels are required"
        } else if (request.ratingLabels.size != 5) {
            errors["ratingLabels"] = "Exactly 5 rating labels are required"
        } else if (request.ratingLabels.any { it.isBlank() }) {
            errors["ratingLabels"] = "Rating labels cannot be blank"
        } else if (request.ratingLabels.any { it.trim().length > 50 }) {
            errors["ratingLabels"] = "Each rating label must be at most 50 characters"
        }

        if (request.thankYouText.isNullOrBlank()) {
            errors["thankYouText"] = "Thank you text is required"
        } else if (request.thankYouText.trim().length > 500) {
            errors["thankYouText"] = "Thank you text must be at most 500 characters"
        }

        if (request.invalidReplyText.isNullOrBlank()) {
            errors["invalidReplyText"] = "Invalid reply text is required"
        } else if (request.invalidReplyText.trim().length > 500) {
            errors["invalidReplyText"] = "Invalid reply text must be at most 500 characters"
        }

        if (request.expiredReplyText.isNullOrBlank()) {
            errors["expiredReplyText"] = "Expired reply text is required"
        } else if (request.expiredReplyText.trim().length > 500) {
            errors["expiredReplyText"] = "Expired reply text must be at most 500 characters"
        }

        if (request.skipForChannels != null && request.skipForChannels.isNotEmpty()) {
            val validChannels = Channel.entries.map { it.name }.toSet()
            val normalized = request.skipForChannels.map { it.trim().uppercase() }
            val invalid = normalized.filter { it !in validChannels }
            if (invalid.isNotEmpty()) {
                errors["skipForChannels"] =
                    "Invalid channels: ${invalid.joinToString()}. Valid values: ${validChannels.joinToString()}"
            }
            if (normalized.size != normalized.toSet().size) {
                errors.putIfAbsent("skipForChannels", "Duplicate channels are not allowed")
            }
        }

        return errors
    }

    private fun toResponse(config: FeedbackFormConfig) = FeedbackFormConfigResponse(
        enterpriseId = config.enterpriseId,
        headerText = config.headerText,
        headerDescription = config.headerDescription,
        footerText = config.footerText,
        ratingLabels = config.ratingLabels,
        thankYouText = config.thankYouText,
        invalidReplyText = config.invalidReplyText,
        expiredReplyText = config.expiredReplyText,
        skipForChannels = config.skipForChannels
    )
}
