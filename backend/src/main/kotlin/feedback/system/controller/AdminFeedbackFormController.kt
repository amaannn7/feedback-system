package feedback.system.controller

import feedback.system.dto.FeedbackFormConfigRequest
import feedback.system.dto.FeedbackFormConfigResponse
import feedback.system.service.FeedbackFormConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/enterprises/{enterpriseId}/session-feedback-form")
class AdminFeedbackFormController(
    private val service: FeedbackFormConfigService
) {

    @GetMapping
    fun getConfig(@PathVariable enterpriseId: String): ResponseEntity<FeedbackFormConfigResponse> {
        val config = service.getConfig(enterpriseId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(config)
    }

    @PutMapping
    fun saveConfig(
        @PathVariable enterpriseId: String,
        @RequestBody request: FeedbackFormConfigRequest
    ): ResponseEntity<FeedbackFormConfigResponse> {
        val saved = service.saveConfig(enterpriseId, request)
        return ResponseEntity.ok(saved)
    }
}
