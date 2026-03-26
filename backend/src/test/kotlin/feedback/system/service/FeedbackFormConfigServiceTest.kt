package feedback.system.service

import feedback.system.dto.FeedbackFormConfigRequest
import feedback.system.repository.FeedbackFormConfigRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class FeedbackFormConfigServiceTest {

    private lateinit var service: FeedbackFormConfigService

    @BeforeEach
    fun setUp() {
        service = FeedbackFormConfigService(mock(FeedbackFormConfigRepository::class.java))
    }

    private fun validRequest() = FeedbackFormConfigRequest(
        headerText = "How was your experience?",
        headerDescription = "Please rate us",
        footerText = "Thank you",
        ratingLabels = listOf("Very Poor", "Poor", "Average", "Good", "Excellent"),
        thankYouText = "Thanks for your feedback!",
        invalidReplyText = "Invalid response",
        expiredReplyText = "This link has expired",
        skipForChannels = listOf("WHATSAPP")
    )

    @Test
    fun `valid request passes validation`() {
        val errors = service.validate(validRequest())
        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
    }

    @Test
    fun `missing headerText fails validation`() {
        val errors = service.validate(validRequest().copy(headerText = null))
        assertTrue(errors.containsKey("headerText"))
        assertEquals("Header text is required", errors["headerText"])
    }

    @Test
    fun `blank headerText fails validation`() {
        val errors = service.validate(validRequest().copy(headerText = "   "))
        assertTrue(errors.containsKey("headerText"))
    }

    @Test
    fun `headerText exceeding 200 chars fails validation`() {
        val longText = "a".repeat(201)
        val errors = service.validate(validRequest().copy(headerText = longText))
        assertTrue(errors.containsKey("headerText"))
        assertEquals("Header text must be at most 200 characters", errors["headerText"])
    }

    @Test
    fun `headerDescription exceeding 500 chars fails validation`() {
        val longText = "a".repeat(501)
        val errors = service.validate(validRequest().copy(headerDescription = longText))
        assertTrue(errors.containsKey("headerDescription"))
    }

    @Test
    fun `null headerDescription is allowed`() {
        val errors = service.validate(validRequest().copy(headerDescription = null))
        assertFalse(errors.containsKey("headerDescription"))
    }

    @Test
    fun `footerText exceeding 200 chars fails validation`() {
        val longText = "a".repeat(201)
        val errors = service.validate(validRequest().copy(footerText = longText))
        assertTrue(errors.containsKey("footerText"))
    }

    @Test
    fun `null ratingLabels fails validation`() {
        val errors = service.validate(validRequest().copy(ratingLabels = null))
        assertTrue(errors.containsKey("ratingLabels"))
        assertEquals("Rating labels are required", errors["ratingLabels"])
    }

    @Test
    fun `wrong number of ratingLabels fails validation`() {
        val errors = service.validate(validRequest().copy(ratingLabels = listOf("A", "B", "C")))
        assertTrue(errors.containsKey("ratingLabels"))
        assertEquals("Exactly 5 rating labels are required", errors["ratingLabels"])
    }

    @Test
    fun `blank ratingLabel fails validation`() {
        val labels = listOf("Very Poor", "Poor", "", "Good", "Excellent")
        val errors = service.validate(validRequest().copy(ratingLabels = labels))
        assertTrue(errors.containsKey("ratingLabels"))
        assertEquals("Rating labels cannot be blank", errors["ratingLabels"])
    }

    @Test
    fun `ratingLabel exceeding 50 chars fails validation`() {
        val labels = listOf("A".repeat(51), "Poor", "Average", "Good", "Excellent")
        val errors = service.validate(validRequest().copy(ratingLabels = labels))
        assertTrue(errors.containsKey("ratingLabels"))
    }

    @Test
    fun `missing thankYouText fails validation`() {
        val errors = service.validate(validRequest().copy(thankYouText = null))
        assertTrue(errors.containsKey("thankYouText"))
    }

    @Test
    fun `missing invalidReplyText fails validation`() {
        val errors = service.validate(validRequest().copy(invalidReplyText = null))
        assertTrue(errors.containsKey("invalidReplyText"))
    }

    @Test
    fun `missing expiredReplyText fails validation`() {
        val errors = service.validate(validRequest().copy(expiredReplyText = null))
        assertTrue(errors.containsKey("expiredReplyText"))
    }

    @Test
    fun `invalid channel name fails validation`() {
        val errors = service.validate(validRequest().copy(skipForChannels = listOf("INVALID_CHANNEL")))
        assertTrue(errors.containsKey("skipForChannels"))
        assertTrue(errors["skipForChannels"]!!.contains("Invalid channels"))
    }

    @Test
    fun `duplicate channels fails validation`() {
        val errors = service.validate(validRequest().copy(skipForChannels = listOf("WHATSAPP", "WHATSAPP")))
        assertTrue(errors.containsKey("skipForChannels"))
    }

    @Test
    fun `valid channels pass validation`() {
        val errors = service.validate(
            validRequest().copy(skipForChannels = listOf("WHATSAPP", "INSTAGRAM", "MESSENGER", "WEB_CHAT"))
        )
        assertFalse(errors.containsKey("skipForChannels"))
    }

    @Test
    fun `empty skipForChannels is allowed`() {
        val errors = service.validate(validRequest().copy(skipForChannels = emptyList()))
        assertFalse(errors.containsKey("skipForChannels"))
    }

    @Test
    fun `null skipForChannels is allowed`() {
        val errors = service.validate(validRequest().copy(skipForChannels = null))
        assertFalse(errors.containsKey("skipForChannels"))
    }

    @Test
    fun `multiple validation errors returned at once`() {
        val request = FeedbackFormConfigRequest(
            headerText = null,
            headerDescription = null,
            footerText = null,
            ratingLabels = null,
            thankYouText = null,
            invalidReplyText = null,
            expiredReplyText = null,
            skipForChannels = null
        )
        val errors = service.validate(request)
        assertTrue(errors.size >= 4, "Expected multiple errors but got: ${errors.size}")
        assertTrue(errors.containsKey("headerText"))
        assertTrue(errors.containsKey("ratingLabels"))
        assertTrue(errors.containsKey("thankYouText"))
        assertTrue(errors.containsKey("invalidReplyText"))
        assertTrue(errors.containsKey("expiredReplyText"))
    }
}
