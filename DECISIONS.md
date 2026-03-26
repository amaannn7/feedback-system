# DECISIONS.md

## Priorities

1. Clean API design
2. Service-layer validation 
3. Complete state machine — PENDING → RESPONDED or      EXPIRED.
4. Simple admin auth

## Assumptions

- MongoDB runs locally.
- Feedback links are single-use (second submit → 409 Conflict).
- Expiration is lazy (checked on access, no background job).
- Auth is API-key based, not production-grade.

## Left Out

Production auth — No OAuth/JWT; just a shared API key.
No Hosting.

