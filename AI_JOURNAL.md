# AI_JOURNAL.md

## Prompts Used

Validation — Design validation for feedback form config with all-at-once error return (not fail-fast).
Frontend — Create admin form config page with field errors, live preview, and channel checkboxes.
Auth + Stats + Link creation — Add API key auth interceptor, feedback request creation endpoint, and analytics dashboard with counts and rating distribution.

## What AI Generated Well

- Project structure (model/dto/service/controller layers)
- Validation logic with comprehensive error messages
- PENDING → RESPONDED/EXPIRED state machine with dual expiration checks
- Frontend components (admin form, star ratings, status cards, stats dashboard)

## One Suggestion I Rejected

Jakarta Bean Validation annotations (`@NotBlank`, `@Size`) on DTOs — rejected in favor of service-layer validation because:
Complex rules ("exactly 5 labels", "no blank items in list", "no duplicate channels") don't fit annotations
Service-layer `validate()` gives full control over error format and is trivially testable

## How I Verified

Reviewed expiration logic (both time-based and status-based checks)
Traced admin PUT → service → repository flow for create vs update handling
Confirmed TypeScript types match backend DTOs
Verified builds: gradlew build + npm run build + npm run lint all clean
