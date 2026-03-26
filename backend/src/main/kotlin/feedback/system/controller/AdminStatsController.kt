package feedback.system.controller

import feedback.system.dto.FeedbackStatsResponse
import feedback.system.service.FeedbackService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/enterprises/{enterpriseId}/stats")
class AdminStatsController(
    private val service: FeedbackService
) {

    @GetMapping
    fun getStats(@PathVariable enterpriseId: String): ResponseEntity<FeedbackStatsResponse> {
        return ResponseEntity.ok(service.getStats(enterpriseId))
    }
}
