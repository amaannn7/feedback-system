package feedback.system.controller

import feedback.system.dto.CreateFeedbackRequestRequest
import feedback.system.dto.CreateFeedbackRequestResponse
import feedback.system.dto.FeedbackRequestListItem
import feedback.system.service.FeedbackService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/enterprises/{enterpriseId}/feedback-requests")
class AdminFeedbackRequestController(
    private val service: FeedbackService
) {

    @GetMapping
    fun list(@PathVariable enterpriseId: String): ResponseEntity<List<FeedbackRequestListItem>> {
        return ResponseEntity.ok(service.listFeedbackRequests(enterpriseId))
    }

    @PostMapping
    fun create(
        @PathVariable enterpriseId: String,
        @RequestBody request: CreateFeedbackRequestRequest
    ): ResponseEntity<CreateFeedbackRequestResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.createFeedbackRequest(enterpriseId, request)
        )
    }
}
