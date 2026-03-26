# AI_JOURNAL.md

## Prompts Used

1. **Architecture** — "Build a full-stack feedback system with Spring Boot + Kotlin + MongoDB backend and Next.js frontend. Admin APIs for form config, public feedback submission, admin UI with preview, star ratings, seed data, tests."
2. **Validation** — "Design validation for feedback form config with all-at-once error return (not fail-fast)."
3. **Frontend** — "Create admin form config page with field errors, live preview, and channel checkboxes."
4. **Error handling** — "GlobalExceptionHandler for 400/401/404/409/410 with consistent ErrorResponse DTO."
5. **Auth + Stats + Link creation** — "Add API key auth interceptor, feedback request creation endpoint, and analytics dashboard with counts and rating distribution."

## What AI Generated Well

- Project structure (model/dto/service/controller layers)
- Validation logic with comprehensive error messages
- PENDING → RESPONDED/EXPIRED state machine with dual expiration checks
- Frontend components (admin form, star ratings, status cards, stats dashboard)
- Test scaffolding with mocks and edge cases

## One Suggestion I Rejected

**Jakarta Bean Validation annotations** (`@NotBlank`, `@Size`) on DTOs — rejected in favor of service-layer validation because:
- Complex rules ("exactly 5 labels", "no blank items in list", "no duplicate channels") don't fit annotations
- Nullable Kotlin DTO fields conflict with annotation-based null handling
- Service-layer `validate()` gives full control over error format and is trivially testable

## How I Verified

- Reviewed expiration logic (both time-based and status-based checks)
- Traced admin PUT → service → repository flow for create vs update handling
- Confirmed TypeScript types match backend DTOs
- Ran all backend tests (JUnit 5 + Mockito) and frontend tests (Jest + Testing Library)
- Verified builds: `gradlew build` + `npm run build` + `npm run lint` all clean
