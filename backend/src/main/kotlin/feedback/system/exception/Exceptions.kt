package feedback.system.exception

class ValidationException(val fieldErrors: Map<String, String>) : RuntimeException("Validation failed")

class NotFoundException(message: String) : RuntimeException(message)

class FeedbackExpiredException(message: String) : RuntimeException(message)

class FeedbackAlreadyRespondedException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)
