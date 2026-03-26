package feedback.system.service

import feedback.system.dto.CreateFeedbackRequestRequest
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

    // --- Create Feedback Request Tests ---

    @Test
    fun `createFeedbackRequest succeeds with valid input`() {
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())
        `when`(feedbackRequestRepo.save(any(FeedbackRequest::class.java))).thenAnswer {
            (it.arguments[0] as FeedbackRequest).copy(id = "new-id")
        }

        val result = service.createFeedbackRequest(
            "enterprise-1",
            CreateFeedbackRequestRequest(sessionId = "session-99", channel = "WEB_CHAT")
        )

        assertEquals("new-id", result.id)
        assertEquals("enterprise-1", result.enterpriseId)
        assertEquals("session-99", result.sessionId)
        assertEquals("WEB_CHAT", result.channel)
        assertEquals("PENDING", result.status)
        assertEquals("/feedback/new-id", result.feedbackUrl)
    }

    @Test
    fun `createFeedbackRequest throws not found for unknown enterprise`() {
        `when`(formConfigRepo.findByEnterpriseId("unknown")).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            service.createFeedbackRequest(
                "unknown",
                CreateFeedbackRequestRequest(sessionId = "s1", channel = "WEB_CHAT")
            )
        }
    }

    @Test
    fun `createFeedbackRequest fails validation with missing fields`() {
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        val ex = assertThrows(ValidationException::class.java) {
            service.createFeedbackRequest(
                "enterprise-1",
                CreateFeedbackRequestRequest(sessionId = null, channel = null)
            )
        }
        assertTrue(ex.fieldErrors.containsKey("sessionId"))
        assertTrue(ex.fieldErrors.containsKey("channel"))
    }

    @Test
    fun `createFeedbackRequest fails validation with invalid channel`() {
        `when`(formConfigRepo.findByEnterpriseId("enterprise-1")).thenReturn(sampleConfig())

        val ex = assertThrows(ValidationException::class.java) {
            service.createFeedbackRequest(
                "enterprise-1",
                CreateFeedbackRequestRequest(sessionId = "s1", channel = "INVALID")
            )
        }
        assertTrue(ex.fieldErrors.containsKey("channel"))
    }

    // --- Stats Tests ---

    @Test
    fun `getStats returns correct counts and average`() {
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.PENDING)).thenReturn(2)
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.RESPONDED)).thenReturn(3)
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.EXPIRED)).thenReturn(1)
        `when`(feedbackRequestRepo.findByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.RESPONDED)).thenReturn(
            listOf(
                pendingRequest().copy(status = FeedbackStatus.RESPONDED, rating = 4),
                pendingRequest().copy(status = FeedbackStatus.RESPONDED, rating = 5),
                pendingRequest().copy(status = FeedbackStatus.RESPONDED, rating = 3)
            )
        )

        val stats = service.getStats("enterprise-1")

        assertEquals("enterprise-1", stats.enterpriseId)
        assertEquals(6, stats.totalCount)
        assertEquals(2, stats.pendingCount)
        assertEquals(3, stats.respondedCount)
        assertEquals(1, stats.expiredCount)
        assertEquals(4.0, stats.averageRating)
        assertEquals(0L, stats.ratingDistribution[1])
        assertEquals(0L, stats.ratingDistribution[2])
        assertEquals(1L, stats.ratingDistribution[3])
        assertEquals(1L, stats.ratingDistribution[4])
        assertEquals(1L, stats.ratingDistribution[5])
    }

    @Test
    fun `getStats returns null average when no responses`() {
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.PENDING)).thenReturn(1)
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.RESPONDED)).thenReturn(0)
        `when`(feedbackRequestRepo.countByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.EXPIRED)).thenReturn(0)
        `when`(feedbackRequestRepo.findByEnterpriseIdAndStatus("enterprise-1", FeedbackStatus.RESPONDED)).thenReturn(emptyList())

        val stats = service.getStats("enterprise-1")

        assertEquals(1, stats.totalCount)
        assertNull(stats.averageRating)
    }

    // --- List Feedback Requests Tests ---

    @Test
    fun `listFeedbackRequests returns sorted list`() {
        val older = pendingRequest("id-1").copy(createdAt = Instant.now().minus(2, ChronoUnit.HOURS))
        val newer = pendingRequest("id-2").copy(createdAt = Instant.now())
        `when`(feedbackRequestRepo.findByEnterpriseId("enterprise-1")).thenReturn(listOf(older, newer))

        val result = service.listFeedbackRequests("enterprise-1")

        assertEquals(2, result.size)
        assertEquals("id-2", result[0].id)
        assertEquals("id-1", result[1].id)
    }
}
