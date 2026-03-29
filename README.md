# ⚡ VoltBook — EV Charging Reservation Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square)
![Azure](https://img.shields.io/badge/Azure-Cloud-0089D6?style=flat-square)
![Twilio](https://img.shields.io/badge/Twilio-SMS-F22F46?style=flat-square)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20RBAC-black?style=flat-square)

A production-oriented REST API backend for an electric vehicle charging reservation platform, targeting Tesla vehicles in Tunisia.

The system handles secure user onboarding, AI-powered vehicle identity verification via OCR, geospatial station discovery, port reservation with OTP confirmation, and NFC-based check-in — all built on a clean, layered Spring Boot architecture with real cloud integrations.

---

## Why This Project Stands Out

This is not a CRUD tutorial.

It demonstrates real-world backend engineering decisions:

- **Cloud document pipeline** — Carte Grise images uploaded to Azure Blob Storage, analyzed by Azure Document Intelligence OCR, with VIN and plate extraction and matching
- **Async event-driven verification** — car verification runs in a background thread via Spring's `@Async` + `TransactionalEventListener`, keeping the API response instant
- **Security-first design** — JWT access + refresh token rotation, BCrypt hashing, role-based access control, and user status gating (INACTIVE → ACTIVE lifecycle)
- **Anti-abuse mechanisms** — OTP resend throttling delegated to Twilio, car verification attempt tracking with 24h block/unblock scheduler
- **Geospatial queries** — PostgreSQL `earthdistance` extension for radius-based station discovery, returning distance-aware payloads
- **Domain-driven architecture** — clean separation of Controllers, Services, Repositories, Domain, DTOs, Cloud integrations, and Security

---

## Product Vision

Electric vehicle charging requires trust, availability, and a smooth user experience.

This backend solves that by combining:

- **Identity and account security** — two-factor account verification (email OTP + SMS via Twilio) with JWT-based stateless auth
- **Vehicle authenticity checks** — Azure OCR on uploaded Carte Grise documents, comparing extracted VIN and plate against user-submitted data
- **Charging infrastructure management** — admin-secured station and port management with geospatial discovery
- **Smart reservation flow** — OTP-confirmed reservations, NFC-based check-in, automated no-show detection, and penalty tracking

---

## Core Features

### 1. Authentication and User Lifecycle
- User registration and login with BCrypt password hashing
- JWT access token (15–30 min expiry) + refresh token (7 days) with rotation on use
- Logout with refresh token revocation
- **Two-factor account verification:**
  - Email OTP via Spring Mail (SMTP)
  - Phone OTP via Twilio SMS
- User status machine: `INACTIVE` → `ACTIVE` after both verifications
- Role-based access control: `ROLE_USER` and `ROLE_ADMIN`
- Custom Spring Security status checker — `INACTIVE` users blocked from protected endpoints

### 2. Car Onboarding and OCR Verification
- Add a car with plate number, chassis number (VIN), and Carte Grise image upload
- Image stored securely in **Azure Blob Storage** (private container, AES-256 encrypted at rest, HTTPS in transit)
- Async verification triggered via `TransactionalEventListener` after DB commit:
  - Image downloaded from Azure Blob
  - Sent to **Azure Document Intelligence** (`prebuilt-read` model)
  - OCR result parsed page-by-page for VIN (17-char regex: `[A-HJ-NPR-Z0-9]{17}`) and Tunisian plate patterns
  - Extracted values compared against user-submitted data
- Verification retry flow with anti-abuse logic:
  - Failed attempts tracked on Car entity
  - Car blocked (`BLOCKED`) after 5 consecutive failures
  - 24-hour unblock via scheduled batch job (every 10 min) + immediate unblock on user request if 24h passed
  - `BlockedCarException` returns hours and minutes remaining

### 3. Station and Port Management (Admin)
- Admin-secured CRUD for stations (name, address, GPS coordinates)
- Port management under each station (label, NFC access identifier, status)
- Port statuses: `AVAILABLE`, `RESERVED`, `OUT_OF_ORDER`
- Cascade deletion: removing a station cancels all port reservations and notifies affected users

### 4. Geospatial Station Discovery
- Find stations within a configurable radius (default 15km) using PostgreSQL `earthdistance` extension
- Native SQL query with `ll_to_earth` and `earth_distance` functions
- Returns distance-aware payload via JPA projection interface (`StationWithDistance`)
- Optional filtering by available ports for a requested time window and duration
- Slot availability engine: generates all full-hour start times for a given date and port, marks each as available or taken using overlap detection:
  ```
  slot.start_time < existing.end_time AND slot.end_time > existing.start_time
  ```

### 5. Reservation Flow (Planned)
- OTP-confirmed reservation creation (anti-abuse gate)
- Fixed duration slots: 6H, 8H, 12H
- Max 2 reservations per car per day, no time overlap
- Reservation state machine: `PENDING_OTP` → `CONFIRMED` → `CHECKED_IN` → `COMPLETED`
- Cancellation with 24h policy: late cancellation and no-show penalties tracked for future billing
- NFC-based check-in: user taps port tag, app sends `accessIdentifier` to backend for validation

### 6. Background Jobs
- **EXPIRED cleaner** — marks unconfirmed OTP reservations as expired after grace period
- **NO_SHOW detector** — marks confirmed reservations as no-show after 30 min grace
- **COMPLETED updater** — marks checked-in reservations as completed when slot ends
- **Car unblock scheduler** — resets blocked cars to UNVERIFIED after 24h (runs every 10 min)

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 3.5 | Framework |
| Spring Security | Authentication, authorization |
| Spring Data JPA | ORM and repository layer |
| Spring Validation | Request validation |
| Spring Mail | Email OTP delivery |
| Spring Async + Scheduling | Background jobs and async verification |

### Data
| Technology | Purpose |
|---|---|
| PostgreSQL | Primary database |
| `earthdistance` + `cube` extensions | Geospatial radius queries |
| Hibernate | ORM dialect |
| HikariCP | Connection pooling |

### Security
| Technology | Purpose |
|---|---|
| JJWT | JWT generation and validation |
| BCrypt | Password hashing |
| Role + Status based authorization | Custom Spring Security policies |

### Cloud & External Services
| Service | Purpose |
|---|---|
| Azure Blob Storage | Carte Grise image storage |
| Azure Document Intelligence | OCR — VIN and plate extraction |
| Twilio | SMS OTP delivery |
| SMTP (Gmail) | Email OTP delivery |

### Tooling
| Tool | Purpose |
|---|---|
| Maven | Build tool |
| Lombok | Boilerplate reduction |
| springdoc OpenAPI | Swagger UI and API docs |

---

## Architecture Overview

```
src/
  main/
    java/com/reservationSys/reservationSys/
      Controllers/         ← REST endpoints
      Services/            ← Business logic and workflows
      Repositories/        ← JPA repositories and native queries
      Domain/              ← JPA entities and enums
      DTOs/                ← Request/response contracts
      Cloud/               ← Azure Blob + OCR integrations
      security/            ← JWT filter, UserDetails, access rules
      exceptions/          ← Centralized API error handling
    resources/
      application.properties
```

Key design decisions:
- **Stateless API** — no server-side sessions, JWT carries identity
- **Async verification** — car OCR runs in a separate thread, user gets instant response
- **TransactionalEventListener** — ensures Azure calls only happen after DB commit succeeds
- **Projection interfaces** — used for geospatial queries to carry computed distance alongside entity fields
- **Single responsibility** — `CarVerificationService`, `CarUpdateService`, `CarSchedulerService` each own one concern to avoid circular dependencies

---

## Key Flows

### User Registration Flow
```
Register → account INACTIVE
→ email OTP sent (Spring Mail)
→ phone OTP sent (Twilio)
→ verify email → emailVerifiedAt set
→ login allowed (INACTIVE users can login after email verify)
→ verify phone → phoneVerifiedAt set → account ACTIVE
→ full access granted
```

### Car Verification Flow
```
POST /car/add
→ car saved as UNVERIFIED
→ Carte Grise uploaded to Azure Blob → URL saved
→ TransactionalEventListener fires after commit
→ async thread: OCR extracts VIN + plate
→ match → VERIFIED | no match → attempt tracked
→ 5 failures → BLOCKED (24h)
→ scheduler or user request unblocks after 24h
```

### Reservation Flow (Planned)
```
User selects car + port + slot
→ POST /reservations → OTP sent to contact phone
→ reservation = PENDING_OTP (port not yet blocked)
→ user enters OTP → CONFIRMED (port blocked)
→ user arrives → taps NFC tag → POST /checkin
→ backend validates reservation → CHECKED_IN
→ slot ends → COMPLETED (port freed)
```

---

## API Reference

### Auth `/api/v1/auth`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | Public | Register new user |
| POST | `/login` | Public | Login, receive JWT tokens |
| POST | `/logout` | Bearer | Revoke refresh token |
| POST | `/refresh` | Public | Rotate refresh token |
| POST | `/verify-email` | Public | Verify email OTP |
| POST | `/verify-phone` | Bearer | Verify phone OTP |
| POST | `/resend-verification-email` | Public | Resend email OTP (2min cooldown) |
| POST | `/resend-verification-phone` | Bearer | Resend phone OTP (2min cooldown) |

### Users `/api/v1/users`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/me` | Bearer | Get current user profile |

### Cars `/api/v1/car`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/add` | Bearer | Add car + trigger async OCR verification |
| GET | `/my-cars` | Bearer | List all user's cars |
| GET | `/{carId}/my-car` | Bearer | Get specific car |
| PUT | `/{carId}/resend-verification` | Bearer | Resubmit Carte Grise image |
| DELETE | `/{carId}/delete` | Bearer | Delete car + cancel reservations |
| DELETE | `/delete-all-cars` | Bearer | Delete all user's cars |

### Stations `/api/v1/stations`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/add-station` | Admin | Create station |
| GET | `/` | Bearer | List all stations |
| GET | `/{id}` | Bearer | Station details with ports |
| GET | `/near-me` | Bearer | Stations within radius (earthdistance) |
| DELETE | `/{id}` | Admin | Delete station + cascade |

### Ports `/api/v1/stations/{stationId}/ports`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Admin | Add port to station |
| GET | `/` | Bearer | List ports with availability |
| GET | `/{portId}/slots` | Bearer | Available time slots for port + date + duration |

---

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL 14+
- Maven
- Azure Storage account + container
- Azure Document Intelligence resource
- Twilio account
- Gmail account with App Password enabled

### 1. Enable PostgreSQL Extensions

Connect to your database and run:
```sql
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
```

### 2. Configure Environment Variables

The application reads all sensitive config from environment variables. Set these in your IDE run configuration or system environment:

```
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=your_256bit_hex_secret
JWT_EXPIRATION=900000
REFRESH_TOKEN_SECRET=your_256bit_hex_secret
REFRESH_TOKEN_EXPIRATION_DAYS=30

TWILIO_ACCOUNT_SID=ACxxxxx
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1xxxxxxxxxx

MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password

AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
AZURE_CONTAINER_NAME=your-container-name
AZURE_DOC_INTELLIGENCE_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
AZURE_DOC_INTELLIGENCE_KEY=your_key

CAR_SCHEDULER_INTERVAL=600000
```

> ⚠️ Never commit real credentials. Use environment variables or a gitignored `application-local.properties`.

### 3. Run the Application

```bash
# With local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Or standard run
./mvnw spring-boot:run
```

### 4. API Documentation

Once running, Swagger UI is available at:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Testing Notes

- Unit and integration tests cover Spring context loading and OCR extraction logic (VIN/plate regex parsing)
- NFC check-in can be tested via Postman by manually sending the `accessIdentifier` value — the endpoint is hardware-agnostic
- OCR verification requires a real Azure Document Intelligence resource — mock the service for unit tests

---

## Roadmap

### Completed
- JWT authentication with access + refresh token rotation
- Two-factor account verification (email OTP + SMS via Twilio)
- Role-based access control (`ROLE_USER`, `ROLE_ADMIN`)
- User status gating (`INACTIVE` → `ACTIVE`)
- Car onboarding with Azure Blob Storage upload
- Async OCR verification via Azure Document Intelligence
- VIN and Tunisian plate extraction with regex
- Car blocking/unblocking with 24h scheduler
- Station and port management (admin-secured)
- Geospatial station discovery with `earthdistance`
- Time-slot availability engine with overlap detection

### In Progress
- Full reservation booking lifecycle
- OTP-confirmed reservation creation
- NFC-based port check-in
- Automated background jobs (no-show detection, slot completion)

### Planned
- Notification log (in-app, SMS, email history)
- Billing and penalty enforcement
- CI/CD pipeline
- Expanded test coverage (integration + e2e)
- Frontend integration (React + Leaflet map)

---

*Built by Mahmoud Zammit — Spring Boot Backend Project — 2026*
