package feedback.system.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController {

    @GetMapping("/verify")
    fun verify(): ResponseEntity<Map<String, Boolean>> {
        // If this endpoint is reached, the interceptor already validated the key
        return ResponseEntity.ok(mapOf("valid" to true))
    }
}
