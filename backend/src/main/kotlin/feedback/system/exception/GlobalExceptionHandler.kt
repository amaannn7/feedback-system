package feedback.system.exception

import feedback.system.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "VALIDATION_ERROR",
                message = "Validation failed",
                fieldErrors = ex.fieldErrors
            )
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(error = "NOT_FOUND", message = ex.message ?: "Resource not found")
        )
    }

    @ExceptionHandler(FeedbackExpiredException::class)
    fun handleExpired(ex: FeedbackExpiredException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.GONE).body(
            ErrorResponse(error = "EXPIRED", message = ex.message ?: "Feedback link has expired")
        )
    }

    @ExceptionHandler(FeedbackAlreadyRespondedException::class)
    fun handleAlreadyResponded(ex: FeedbackAlreadyRespondedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(error = "ALREADY_RESPONDED", message = ex.message ?: "Feedback already submitted")
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleBadRequest(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(error = "BAD_REQUEST", message = "Invalid request body")
        )
    }
}
