# Chat Session Feedback System

Full-stack system for collecting chat session feedback via unique, time-limited links. Admins configure feedback forms, create & send feedback links, and view analytics. Customers rate 1-5 stars.

## Tech Stack

- **Backend:** Spring Boot 4 + Kotlin + MongoDB
- **Frontend:** Next.js 16 + React 19 + Tailwind CSS 4

## Quick Start

# 1. MongoDB
docker run -d -p 27017:27017 --name feedback-mongo mongo:7

# 2. Backend (http://localhost:8080)
cd backend
./gradlew bootRun       # or gradlew.bat on Windows

# 3. Frontend (http://localhost:3000)
cd frontend
npm run dev


Open http://localhost:3000 -- log into admin with key `demo-admin-key`

## Features

- **Admin auth** -- API key-gated admin endpoints (`X-Admin-Key` header, verified server-side)
- **Form config** -- GET/PUT enterprise feedback form with full validation
- **Feedback link creation** -- POST to create a feedback request, returns a shareable URL (24h expiry)
- **Public feedback page** -- Star rating UI with expired/responded/error states
- **Analytics dashboard** -- Counts, average rating, rating distribution, request table
- **Seed data** -- Demo config + 3 feedback requests auto-created on first boot

## Tests

```bash
cd backend && ./gradlew test
cd frontend && npm test
```

## API Endpoints

All admin endpoints require `X-Admin-Key` header. Returns 401 if invalid.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/auth/verify` | Verify admin API key |
| GET | `/api/admin/enterprises/{id}/session-feedback-form` | Get form config |
| PUT | `/api/admin/enterprises/{id}/session-feedback-form` | Save form config |
| GET | `/api/admin/enterprises/{id}/feedback-requests` | List feedback requests |
| POST | `/api/admin/enterprises/{id}/feedback-requests` | Create feedback request |
| GET | `/api/admin/enterprises/{id}/stats` | Get analytics |
| GET | `/api/public/feedback/{feedbackId}` | Load feedback page data |
| POST | `/api/public/feedback/{feedbackId}/respond` | Submit rating (1-5) |

## Error Codes

| Status | Code | When |
|--------|------|------|
| 400 | VALIDATION_ERROR | Invalid input |
| 401 | UNAUTHORIZED | Bad/missing admin key |
| 404 | NOT_FOUND | Unknown resource |
| 409 | ALREADY_RESPONDED | Feedback already submitted |
| 410 | EXPIRED | Feedback link expired |
