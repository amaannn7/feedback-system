# AI_JOURNAL.md

## Representative Prompts Used

### 1. Architecture Planning
> "Build a full-stack chat session feedback system with Spring Boot + Kotlin backend using MongoDB, and Next.js frontend. The system needs admin APIs for feedback form configuration (GET/PUT), a public feedback submission endpoint, admin UI with live preview, and a public feedback page with star ratings. Include seed data and tests."

This was the main prompt that generated the overall architecture. I provided the full spec and let the AI scaffold the project structure.

### 2. Validation Design
> "Design validation rules for the feedback form config: headerText required max 200, headerDescription optional max 500, ratingLabels exactly 5 non-blank items max 50 each, skipForChannels must be valid channel names with no duplicates. Return all errors at once, not fail-fast."

Used to generate the `validate()` method in FeedbackFormConfigService with comprehensive error checking.

### 3. Frontend State Management
> "Create a Next.js client component for admin feedback form configuration. It should load existing config on mount, display a form with all fields including 5 fixed rating label inputs and channel checkboxes, handle save with validation error display, and show a toggleable preview panel."

Generated the admin page component with form state management, fetch calls, and the preview panel.

### 4. Error Handling Strategy
> "Implement a GlobalExceptionHandler for Spring Boot that handles ValidationException (400), NotFoundException (404), FeedbackExpiredException (410), and FeedbackAlreadyRespondedException (409) with a consistent ErrorResponse DTO."

Generated the exception hierarchy and handler with appropriate HTTP status codes.

### 5. Test Generation
> "Write JUnit 5 tests for FeedbackFormConfigService.validate() covering: valid request passes, missing required fields, exceeding max lengths, wrong number of rating labels, blank labels, invalid channels, duplicate channels, and multiple simultaneous errors."

Generated comprehensive validation test cases.

## What AI Generated That Was Useful

- **Project structure and boilerplate**: The overall file organization (model/dto/service/controller/exception layers) was well-structured and saved significant time.
- **Validation logic**: The `validate()` method covering all field rules with clear error messages was generated accurately.
- **State machine for feedback requests**: The PENDING → RESPONDED/EXPIRED transitions with both status-based and time-based expiration checks.
- **Frontend components**: The admin form with field error display, the star rating component on the public page, and the status cards for different feedback states.
- **Test scaffolding**: Both service test classes with mock setup and comprehensive test cases.

## One AI Suggestion I Rejected and Why

The AI initially suggested using **Jakarta Bean Validation annotations** (`@NotBlank`, `@Size`, etc.) on the DTO fields for validation. I rejected this approach and chose service-layer validation instead because:

1. **Complex rules can't use annotations alone**: The "exactly 5 rating labels" rule, "no blank items in a list", and "no duplicate channel names" don't map cleanly to standard validation annotations and would require custom validators — adding complexity without benefit.
2. **Null handling in Kotlin DTOs**: Making DTO fields nullable for proper "missing field" detection conflicts with annotation-based validation (which treats null as valid for most annotations unless combined with `@NotNull`). Service-layer validation handles this more naturally.
3. **Consistent error format**: With annotation-based validation, errors come from `MethodArgumentNotValidException` and need to be reformatted. With service-layer validation, I control the exact error format from the start.
4. **Testability**: The `validate()` method is a pure function that's trivially testable without Spring context — just pass a request, get errors back.

## How I Validated and Corrected AI-Generated Code

1. **Manual code review**: Read through all generated code to verify correctness, especially the expiration logic in `FeedbackService` where PENDING requests need both time-based and status-based expiration checks.

2. **Traced the data flow**: Validated that the admin PUT → service.saveConfig → repository.save chain correctly handles both create (no existing config) and update (existing config with preserved `createdAt`) cases.

3. **Verified error handling paths**: Confirmed that every error case in the public respond endpoint (invalid rating, not found, expired, already responded) returns the correct HTTP status code and error format.

4. **Checked frontend-backend contract**: Ensured the TypeScript types in `lib/types.ts` match the backend DTO shapes, and that the frontend correctly handles all response status values.

5. **Reviewed test coverage**: Verified that tests cover the key validation rules and service behaviors, not just happy paths. Added tests for multiple simultaneous errors and boundary conditions.
