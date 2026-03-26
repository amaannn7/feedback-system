# DECISIONS.md

## Priorities

1. **Clean API design** — Consistent `ErrorResponse` format with error codes, messages, and per-field validation errors across all endpoints.
2. **Service-layer validation** — All rules in `validate()` methods, returning all errors at once (not fail-fast). Trivially testable without Spring context.
3. **Complete state machine** — PENDING → RESPONDED or EXPIRED. Expiration checked both by stored status and `expiresAt` timestamp, no background job needed.
4. **Simple admin auth** — API key in `X-Admin-Key` header, verified by interceptor. Key stored in `application.properties`. Frontend verifies key against backend before storing in sessionStorage.

## Assumptions

- MongoDB runs locally (Docker command in README).
- Channels are a fixed enum (WHATSAPP, INSTAGRAM, MESSENGER, WEB_CHAT).
- Feedback links are single-use (second submit → 409 Conflict).
- Expiration is lazy (checked on access, no background job).
- Auth is API-key based, not production-grade.

## Left Out

- **Double-submit protection** — No optimistic locking; second click returns 409.
- **Background expiration** — Requests only expire when accessed.
- **Docker Compose** — No multi-service containerized setup.
- **Production auth** — No OAuth/JWT; just a shared API key.

## Next Steps

1. Docker Compose for one-command startup
2. Optimistic locking on feedback submission
3. Integration tests with Testcontainers
4. Accessibility (keyboard nav for star rating, ARIA labels)
