# DECISIONS.md

## What I Prioritized

1. **Clean API design** — RESTful endpoints with consistent error responses using a unified `ErrorResponse` format with error codes, human-readable messages, and per-field errors for validation failures.

2. **Correct validation** — All validation rules are implemented in the service layer for maximum control. The `validate()` method returns all errors at once (not fail-fast), so the UI can display all problems simultaneously.

3. **Working end-to-end flow** — Both the admin configuration flow and the public feedback submission flow are fully functional, including all edge states (expired, already responded, not found).

4. **Testability** — The validation logic is a public method on the service, making it trivially testable without mocks. Service tests use standard Mockito for dependency isolation.

5. **Clear state management** — Feedback requests have three explicit states: PENDING → RESPONDED or EXPIRED. The expiration check is done both by the stored status field and by comparing the `expiresAt` timestamp, ensuring correctness even if no background job runs.

## Assumptions Made

- **No authentication**: Admin endpoints are open. In production, these would sit behind enterprise-scoped authentication.
- **MongoDB runs locally**: No containerized setup by default; a Docker command is documented in the README.
- **Feedback request creation is out of scope**: The system assumes feedback requests are created by another service. Only seed data provides test requests.
- **Channel names are a fixed enum**: WHATSAPP, INSTAGRAM, MESSENGER, WEB_CHAT. Adding new channels requires a code change.
- **Single-use feedback links**: Each feedback request can only be responded to once. No idempotency key is used — a second submission returns 409 Conflict.
- **Expiration is checked on access**: No background scheduled job expires requests. Instead, expiration is checked when the feedback link is accessed or when a rating is submitted.

## What I Intentionally Left Out

- **Authentication/Authorization**: Entirely skipped per the spec's "out of scope" guidance. In production, admin endpoints would require enterprise-scoped tokens and the public endpoint would remain unauthenticated.
- **Idempotency**: Double-submit protection is not implemented. Two rapid clicks could theoretically race, though the second would fail with 409. A proper solution would use optimistic locking (MongoDB version field) or an idempotency key header.
- **Advanced statistics**: No analytics endpoint or dashboard. The repository has a `countByEnterpriseIdAndStatus` method that could support basic stats.
- **Background expiration job**: Expired requests are only marked when accessed. A scheduled task would be better for consistency.
- **Feedback request creation API**: No endpoint to create new feedback requests. In a real system, this would be triggered when a chat session ends.
- **Containerization**: No Docker Compose setup. MongoDB is expected to run locally.

## What I Would Do Next With Another Half Day

1. **Docker Compose setup** — Single `docker-compose up` to start MongoDB + backend + frontend.
2. **Idempotency for submissions** — Use MongoDB's `findAndModify` with a status precondition to prevent race conditions on double-submit.
3. **Admin statistics dashboard** — Show total feedback received, average rating, and ratings distribution per enterprise.
4. **Feedback request creation API** — POST endpoint to create new feedback requests when a chat session ends.
5. **Integration tests** — Full API tests using Testcontainers for MongoDB, testing the complete request → response cycle.
6. **Accessibility improvements** — Keyboard navigation for the star rating, ARIA attributes, focus management.
7. **API versioning and documentation** — OpenAPI/Swagger spec for the API.
