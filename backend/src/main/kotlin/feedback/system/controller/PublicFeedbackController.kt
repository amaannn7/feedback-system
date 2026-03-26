package feedback.system.controller

import feedback.system.dto.FeedbackPageData
import feedback.system.dto.FeedbackRespondRequest
import feedback.system.dto.FeedbackRespondResponse
import feedback.system.service.FeedbackService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/feedback")
class PublicFeedbackController(
    private val service: FeedbackService
) {

    @GetMapping("/{feedbackId}")
    fun getFeedbackPage(@PathVariable feedbackId: String): ResponseEntity<FeedbackPageData> {
        return ResponseEntity.ok(service.getFeedbackPageData(feedbackId))
    }

    @PostMapping("/{feedbackId}/respond")
    fun respond(
        @PathVariable feedbackId: String,
        @RequestBody request: FeedbackRespondRequest
    ): ResponseEntity<FeedbackRespondResponse> {
        return ResponseEntity.ok(service.submitFeedback(feedbackId, request))
    }
}
