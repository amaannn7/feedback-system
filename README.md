# Chat Session Feedback Mini-System

A full-stack feedback system where customers rate their chat interactions (1–5) through unique feedback links, and enterprise admins configure feedback form appearance.

## Tech Stack

- **Backend:** Spring Boot 4 + Kotlin + MongoDB
- **Frontend:** Next.js 16 + React 19 + Tailwind CSS 4
- **Persistence:** MongoDB

## Prerequisites

- **Java 17+** (for Spring Boot backend)
- **Node.js 20+** (for Next.js frontend)
- **MongoDB** running on `localhost:27017`

## Quick Start

### 1. Start MongoDB

```bash
# If installed locally
mongod

# Or with Docker
docker run -d -p 27017:27017 --name feedback-mongo mongo:7
```

### 2. Start Backend

```bash
cd backend
./gradlew bootRun       # macOS/Linux
gradlew.bat bootRun     # Windows
```

Backend starts at **http://localhost:8080**. Demo data is auto-seeded on first run.

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at **http://localhost:3000**.

### 4. Test the Application

Open http://localhost:3000 — you'll see:
- **Admin page** link for `demo-enterprise`
- **Feedback page** links in 3 states: valid, expired, already used

## Running Tests

### Backend Tests

```bash
cd backend
./gradlew test          # macOS/Linux
gradlew.bat test        # Windows
```

### Frontend Tests

```bash
cd frontend
npm test
```

## API Documentation

### Admin APIs

#### GET /api/admin/enterprises/{enterpriseId}/session-feedback-form

Returns the feedback form configuration for an enterprise.

**Response 200:**
```json
{
  "enterpriseId": "demo-enterprise",
  "headerText": "How was your experience?",
  "headerDescription": "We'd love to hear about your recent chat interaction.",
  "footerText": "Your feedback is anonymous and appreciated.",
  "ratingLabels": ["Very Poor", "Poor", "Average", "Good", "Excellent"],
  "thankYouText": "Thank you for your feedback!",
  "invalidReplyText": "Sorry, that's not a valid response.",
  "expiredReplyText": "Sorry, this feedback link has expired.",
  "skipForChannels": []
}
```

**Response 404:** No configuration exists for this enterprise.

#### PUT /api/admin/enterprises/{enterpriseId}/session-feedback-form

Create or update the feedback form configuration.

**Request Body:**
```json
{
  "headerText": "How was your experience?",
  "headerDescription": "Please rate your recent interaction.",
  "footerText": "Your feedback is anonymous.",
  "ratingLabels": ["Very Poor", "Poor", "Average", "Good", "Excellent"],
  "thankYouText": "Thank you for your feedback!",
  "invalidReplyText": "Please select a rating from 1 to 5.",
  "expiredReplyText": "This feedback link has expired.",
  "skipForChannels": ["WHATSAPP"]
}
```

**Response 200:** Updated configuration (same shape as GET).

**Response 400:** Validation errors:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "fieldErrors": {
    "headerText": "Header text is required",
    "ratingLabels": "Exactly 5 rating labels are required"
  }
}
```

### Public APIs

#### GET /api/public/feedback/{feedbackId}

Load feedback request data for the public page.

**Response 200 (pending):**
```json
{
  "feedbackId": "feedback-valid",
  "status": "PENDING",
  "form": {
    "headerText": "How was your experience?",
    "headerDescription": "...",
    "footerText": "...",
    "ratingLabels": ["Very Poor", "Poor", "Average", "Good", "Excellent"]
  },
  "message": null
}
```

**Response 200 (expired/responded):**
```json
{
  "feedbackId": "feedback-expired",
  "status": "EXPIRED",
  "form": null,
  "message": "Sorry, this feedback link has expired."
}
```

#### POST /api/public/feedback/{feedbackId}/respond

Submit a rating for a feedback request.

**Request Body:**
```json
{
  "rating": 4
}
```

**Response 200:**
```json
{
  "feedbackId": "feedback-valid",
  "status": "RESPONDED",
  "message": "Thank you for your feedback!"
}
```

**Error Responses:**
| Status | Error Code | Condition |
|--------|-----------|-----------|
| 400 | VALIDATION_ERROR | Rating is null or not 1–5 |
| 404 | NOT_FOUND | Unknown feedbackId |
| 409 | ALREADY_RESPONDED | Feedback already submitted |
| 410 | EXPIRED | Feedback link expired |

## Validation Rules

| Field | Rules |
|-------|-------|
| headerText | Required, non-blank, max 200 characters |
| headerDescription | Optional (nullable), max 500 characters if provided |
| footerText | Optional (nullable), max 200 characters if provided |
| ratingLabels | Required, exactly 5 items, each non-blank, max 50 chars each |
| thankYouText | Required, non-blank, max 500 characters |
| invalidReplyText | Required, non-blank, max 500 characters |
| expiredReplyText | Required, non-blank, max 500 characters |
| skipForChannels | Optional, valid values: WHATSAPP, INSTAGRAM, MESSENGER, WEB_CHAT, no duplicates |

## Demo Data

Automatically seeded on first backend startup:

| ID | Type | State |
|----|------|-------|
| `demo-enterprise` | Enterprise config | Fully configured feedback form |
| `feedback-valid` | Feedback request | PENDING, expires in 24h |
| `feedback-expired` | Feedback request | EXPIRED |
| `feedback-used` | Feedback request | RESPONDED (rating: 4) |

## Assumptions

- No authentication is required for admin endpoints (out of scope per spec)
- MongoDB runs locally without authentication
- Channel values are case-insensitive on input, stored uppercase
- Blank optional string fields are normalized to null
- Feedback requests are created externally (the creation mechanism is out of scope)
- The frontend development server proxies API calls to the backend via Next.js rewrites
