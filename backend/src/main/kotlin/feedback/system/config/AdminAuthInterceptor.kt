package feedback.system.config

import feedback.system.exception.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAuthInterceptor(
    @Value("\${admin.api-key}") private val adminApiKey: String
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val apiKey = request.getHeader("X-Admin-Key")
        if (apiKey == null || apiKey != adminApiKey) {
            throw UnauthorizedException("Invalid or missing admin API key")
        }
        return true
    }
}
