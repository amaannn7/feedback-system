package feedback.system.service

import feedback.system.dto.FeedbackRespondRequest
import feedback.system.exception.FeedbackAlreadyRespondedException
import feedback.system.exception.FeedbackExpiredException
import feedback.system.exception.NotFoundException
import feedback.system.exception.ValidationException
import feedback.system.model.FeedbackFormConfig
import feedback.system.model.FeedbackRequest
import feedback.system.model.FeedbackStatus
import feedback.system.repository.FeedbackFormConfigRepository
import feedback.system.repository.FeedbackRequestRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock
    lateinit var feedbackRequestRepo: FeedbackRequestRepository

    @Mock
    lateinit var formConfigRepo: FeedbackFormConfigRepository

    private lateinit var service: FeedbackService

    @BeforeEach
    fun setUp() {
        service = FeedbackService(feedbackRequestRepo, formConfigRepo)
    }

    private fun pendingRequest(id: String = "test-id") = FeedbackRequest(
        id = id,
        enterpriseId = "enterprise-1",
        sessionId = "session-1",
        channel = "WEB_CHAT",
        status = FeedbackStatus.PENDING,
        expiresAt = Instant.now().plus(24, ChronoUnit.HOURS)
    )

    private fun sampleConfig() = FeedbackFormConfig(
        id = "config-1",
        enterpriseId = "enterprise-1",
        headerText = "Rate us",
        ratingLabels = listOf("1", "2", "3", "4", "5"),
        thankYouText = "Thank you!",
        invalidReplyText = "Invalid",
        expiredReplyText = "Expired"
    )

    @Test
    fun `submit valid rating succeeds`() {
        val request = pendingRequest()
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(request))
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())
        `when`(feedbackRequestRepo.save(any(FeedbackRequest::class.java))).thenAnswer { it.arguments[0] }

        val result = service.submitFeedback("test-id", FeedbackRespondRequest(4))

        assertEquals("RESPONDED", result.status)
        assertEquals("Thank you!", result.message)
        assertEquals("test-id", result.feedbackId)
    }

    @Test
    fun `submit with null rating fails validation`() {
        assertThrows(ValidationException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(null))
        }
    }

    @Test
    fun `submit with rating below 1 fails validation`() {
        assertThrows(ValidationException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(0))
        }
    }

    @Test
    fun `submit with rating above 5 fails validation`() {
        assertThrows(ValidationException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(6))
        }
    }

    @Test
    fun `submit to unknown feedbackId throws not found`() {
        `when`(feedbackRequestRepo.findById("unknown")).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.submitFeedback("unknown", FeedbackRespondRequest(3))
        }
    }

    @Test
    fun `submit to expired feedback throws expired exception`() {
        val expiredRequest = pendingRequest().copy(
            status = FeedbackStatus.EXPIRED,
            expiresAt = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(expiredRequest))
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        assertThrows(FeedbackExpiredException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(3))
        }
    }

    @Test
    fun `submit to pending but time-expired feedback throws expired exception`() {
        val timeExpired = pendingRequest().copy(
            expiresAt = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(timeExpired))
        `when`(feedbackRequestRepo.save(any(FeedbackRequest::class.java))).thenAnswer { it.arguments[0] }
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        assertThrows(FeedbackExpiredException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(3))
        }
    }

    @Test
    fun `submit to already responded feedback throws already responded`() {
        val responded = pendingRequest().copy(
            status = FeedbackStatus.RESPONDED,
            rating = 5,
            respondedAt = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(responded))

        assertThrows(FeedbackAlreadyRespondedException::class.java) {
            service.submitFeedback("test-id", FeedbackRespondRequest(3))
        }
    }

    @Test
    fun `getFeedbackPageData returns PENDING for valid request`() {
        val request = pendingRequest()
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(request))
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        val data = service.getFeedbackPageData("test-id")

        assertEquals("PENDING", data.status)
        assertNotNull(data.form)
        assertEquals("Rate us", data.form!!.headerText)
    }

    @Test
    fun `getFeedbackPageData returns EXPIRED for expired request`() {
        val expired = pendingRequest().copy(status = FeedbackStatus.EXPIRED)
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(expired))
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        val data = service.getFeedbackPageData("test-id")

        assertEquals("EXPIRED", data.status)
        assertNull(data.form)
        assertNotNull(data.message)
    }

    @Test
    fun `getFeedbackPageData returns RESPONDED for used request`() {
        val responded = pendingRequest().copy(status = FeedbackStatus.RESPONDED)
        `when`(feedbackRequestRepo.findById("test-id")).thenReturn(Optional.of(responded))
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        val data = service.getFeedbackPageData("test-id")

        assertEquals("RESPONDED", data.status)
        assertNull(data.form)
    }

    @Test
    fun `getFeedbackPageData throws not found for unknown id`() {
        `when`(feedbackRequestRepo.findById("unknown")).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.getFeedbackPageData("unknown")
        }
    }
}
