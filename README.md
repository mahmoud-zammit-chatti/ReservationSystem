# EV Charging Reservation System - Backend

A production-oriented backend for an electric vehicle charging reservation platform.

This project provides secure user onboarding, car identity verification using OCR, station/port management, and location-aware station discovery.  
It is designed as a scalable Spring Boot API with cloud integrations and clean service-layer architecture.

---

## Product Vision

Electric vehicle charging requires trust, availability, and smooth user onboarding.  
This backend solves that by combining:

- **Identity and account security** (JWT + refresh tokens, role/status checks)
- **Vehicle authenticity checks** (Azure OCR on uploaded registration cards)
- **Charging infrastructure management** (stations and ports)
- **Smart discovery** (nearby station search and time-slot aware availability)

The goal is to serve as the core platform API for an EV reservation product used by drivers and operators.

---

## Core Features

### 1) Authentication and User Lifecycle
- User registration and login
- JWT access token + refresh token flow
- Logout + refresh token rotation
- Email verification (OTP via email)
- Phone verification (OTP via SMS/Twilio)
- User status transitions (`INACTIVE` -> `ACTIVE`) after verification

### 2) Car Onboarding and Verification
- Add a car with:
  - Plate number
  - Chassis number (VIN)
  - Carte grise image upload
- Store uploaded documents in **Azure Blob Storage**
- Verify vehicle identity using **Azure Document Intelligence (OCR)**
- Automatic matching:
  - OCR-extracted VIN vs submitted chassis number
  - OCR-extracted plate vs submitted plate number
- Retry verification flow with anti-abuse logic:
  - Failed attempts tracked
  - Car blocked after repeated failures
  - 24-hour unblock strategy (manual check + scheduler)

### 3) Station and Port Management
- Admin-secured station creation/deletion
- Port creation under a station
- Station details with attached ports

### 4) Geospatial Discovery
- Find stations within radius from user coordinates
- Optional filtering by available ports for a requested time window and duration
- Distance-aware response payload for client apps

### 5) API Documentation and Error Handling
- OpenAPI/Swagger UI integration
- Structured global error responses with HTTP status mapping
- Validation error detail support

---

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.5**
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation

### Data
- **PostgreSQL**
- Native SQL for geospatial distance query (`earth_distance`, `ll_to_earth`)

### Security
- **JWT** (`jjwt`)
- BCrypt password hashing
- Role-based and status-based authorization policies

### Cloud / External Services
- **Azure Blob Storage** (document upload/storage)
- **Azure Document Intelligence (OCR)** (carte grise text extraction)
- **Twilio** (SMS OTP delivery)
- **SMTP (Spring Mail)** (email OTP delivery)

### Tooling
- Maven
- Lombok
- springdoc OpenAPI (Swagger)

---

## Architecture Overview

The codebase follows a layered Spring architecture:

- `Controllers` - REST endpoints
- `Services` - business logic and workflows
- `Repositories` - persistence and queries
- `Domain` - JPA entities and enums
- `DTOs` - request/response contracts
- `Cloud` - Azure integration services
- `security` - JWT filter, access rules, user details
- `exceptions` - centralized API error handling

Main application class enables:
- async processing (`@EnableAsync`)
- scheduled jobs (`@EnableScheduling`)

---

## Key Flows

### User Registration Flow
1. User registers
2. Account is created as inactive
3. OTPs are generated (email + phone purposes)
4. SMS and email are sent
5. User verifies email + phone
6. Account becomes active and can access protected features

### Car Verification Flow
1. User uploads car data + registration image
2. Image stored in Azure Blob
3. Async verification event is published
4. OCR extracts VIN and plate text
5. Matching logic determines verification result
6. Car status updated (`VERIFIED`, retry count, or `BLOCKED`)

---

## API Surface (High-Level)

### Auth (`/api/v1/auth`)
- `POST /register`
- `POST /login`
- `POST /logout`
- `POST /refresh`
- `POST /verify-email`
- `POST /resend-verification-email`
- `POST /verify-phone` (authenticated)
- `POST /resend-verification-phone` (authenticated)

### User (`/api/v1/users`)
- `GET /me`

### Cars (`/api/v1/car`)
- `POST /add` (multipart)
- `PUT /{carId}/resend-verification` (multipart)
- `GET /my-cars`
- `GET /{carId}/my-car`
- `DELETE /delete-all-cars`
- `DELETE /{carId}/delete`

### Stations (`/api/v1/stations`)
- `POST /add-station` (admin)
- `GET /`
- `GET /{id}`
- `GET /{id}/ports`
- `GET /near-me`
- `DELETE /{id}` (admin)

### Ports
- `POST /api/v1/stations/{stationId}/ports`

---

## Project Structure

```text
src/
  main/
    java/com/reservationSys/reservationSys/
      Cloud/
      Controllers/
      Domain/
      DTOs/
      Repositories/
      Services/
      Swagger/
      security/
      exceptions/
    resources/
      application.properties
      application-local.properties
  test/
    java/com/reservationSys/reservationSys/
```

---

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL
- Maven (or use wrapper)
- Azure Storage account + container
- Azure Document Intelligence resource
- Twilio account
- SMTP credentials

### 1) Configure Environment Variables

Set variables used by `src/main/resources/application.properties`:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `REFRESH_TOKEN_EXPIRATION_DAYS`
- `REFRESH_TOKEN_SECRET`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_PHONE_NUMBER`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `AZURE_STORAGE_CONNECTION_STRING`
- `AZURE_CONTAINER_NAME`
- `AZURE_DOC_INTELLIGENCE_ENDPOINT`
- `AZURE_DOC_INTELLIGENCE_KEY`
- `CAR_SCHEDULER_INTERVAL`

> Recommendation: keep secrets outside source control and rotate any previously exposed credentials.

### 2) Database Notes

This project uses PostgreSQL geospatial distance functions in native SQL.  
Enable required extensions in your DB if needed:

```sql
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
```

### 3) Run the Application

```bash
./mvnw spring-boot:run
```

Or with local profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 4) API Docs

Once running, open:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

---

## Testing

Run all tests:

```bash
./mvnw test
```

Current tests include:
- Spring context load test
- OCR extraction-focused tests for VIN/plate parsing behavior

---

## Security and Reliability Highlights

- Stateless JWT authentication
- Refresh-token revocation/rotation model
- Role checks (admin-protected station mutations)
- Status checks (email verified / active user gating)
- OTP resend throttling for abuse reduction
- Centralized exception-to-response mapping
- Scheduled unblock job for blocked car lifecycle

---

## Current Scope and Roadmap

### Implemented
- Auth + verification stack
- Car onboarding + OCR verification
- Station/port management
- Nearby station lookup and availability filtering foundation

### Planned / Next
- Full reservation booking APIs and lifecycle endpoints
- Notification log expansion (in-app/email/SMS history)
- Stronger observability (structured logs, metrics, tracing)
- CI/CD and deployment hardening
- Expanded integration and end-to-end test coverage

---

## Why This Project Stands Out

This is not just CRUD.  
It demonstrates real-world backend engineering with:

- Cloud document pipeline integration (Blob + OCR)
- Async event-driven verification
- Security-first authentication and authorization
- External service orchestration (Twilio, SMTP, Azure)
- Domain-oriented architecture ready for scaling

---


