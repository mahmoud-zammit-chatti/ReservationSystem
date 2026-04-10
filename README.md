# ⚡ VoltBook — EV Charging Reservation Backend

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Azure](https://img.shields.io/badge/Azure-Blob%20%2B%20OCR-blue) ![Twilio](https://img.shields.io/badge/Twilio-SMS-red) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

A production-oriented REST API backend for an electric vehicle charging reservation platform, targeting Tesla vehicles in Tunisia.

The system handles secure user onboarding, AI-powered vehicle identity verification via OCR, geospatial station discovery, port reservation with OTP confirmation, and NFC-based check-in — all built on a clean, layered Spring Boot architecture with real cloud integrations.

---

## Why This Project Stands Out

This is not a CRUD tutorial.

It demonstrates real-world backend engineering decisions:

- **Cloud document pipeline** — Carte Grise images uploaded to Azure Blob Storage, analyzed by Azure Document Intelligence OCR, with VIN and plate extraction and matching
- **Async event-driven verification** — car verification runs in a background thread via Spring's `@Async` + `TransactionalEventListener`, keeping the API response instant
- **Security-first design** — JWT access + refresh token rotation, BCrypt hashing, role-based access control, and user status gating (`INACTIVE → ACTIVE` lifecycle)
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
- Two-factor account verification:
  - Email OTP via Spring Mail (SMTP)
  - Phone OTP via Twilio SMS
- User status machine: `INACTIVE → ACTIVE` after both verifications
- Role-based access control: `ROLE_USER` and `ROLE_ADMIN`
- Custom Spring Security status checker — `INACTIVE` users blocked from protected endpoints

### 2. Car Onboarding and OCR Verification
- Add a car with plate number, chassis number (VIN), and Carte Grise image upload
- Image stored securely in Azure Blob Storage (private container, AES-256 encrypted at rest, HTTPS in transit)
- Async verification triggered via `TransactionalEventListener` after DB commit:
  - Image downloaded from Azure Blob
  - Sent to Azure Document Intelligence (`prebuilt-read` model)
  - OCR result parsed page-by-page for VIN (`17-char regex: [A-HJ-NPR-Z0-9]{17}`) and Tunisian plate patterns
  - Extracted values compared against user-submitted data
- Verification retry flow with anti-abuse logic:
  - Failed attempts tracked on `Car` entity
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

### 5. Reservation Flow
- OTP-confirmed reservation creation (anti-abuse gate)
- Fixed duration slots: 6H, 8H, 12H
- Max 2 reservations per car per day, no time overlap
- Reservation state machine: `PENDING_OTP → CONFIRMED → CHECKED_IN → COMPLETED`
- Cancellation with 24h policy: late cancellation and no-show penalties tracked for future billing
- NFC-based check-in: user taps port tag, app sends `accessIdentifier` to backend for validation

### 6. Background Jobs
- **EXPIRED cleaner** — marks unconfirmed OTP reservations as expired after grace period
- **NO_SHOW detector** — marks confirmed reservations as no-show after 30 min grace
- **COMPLETED updater** — marks checked-in reservations as completed when slot ends
- **Car unblock scheduler** — resets blocked cars to `UNVERIFIED` after 24h (runs every 10 min)

---

## Tech Stack

### Backend
| Technology                | Purpose |
|---------------------------|---|
| Java 25                   | Language |
| Spring Boot 3.5           | Framework |
| Spring Security           | Authentication, authorization |
| Spring Data JPA           | ORM and repository layer |
| Spring Validation         | Request validation |
| Spring Mail               | Email OTP delivery |
| Spring Async + Scheduling | Background jobs and async verification |

### Data
| Technology | Purpose |
|---|---|
| PostgreSQL | Primary database |
| earthdistance + cube extensions | Geospatial radius queries |
| Hibernate | ORM dialect |
| HikariCP | Connection pooling |
| Flyway | Database migrations |

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
| Docker + Compose | Containerization and local orchestration |

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
      db/migration/        ← Flyway SQL migrations
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

### Reservation Flow
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
|---|---|---|---|
| POST | /register | Public | Register new user |
| POST | /login | Public | Login, receive JWT tokens |
| POST | /logout | Bearer | Revoke refresh token |
| POST | /refresh | Public | Rotate refresh token |
| POST | /verify-email | Public | Verify email OTP |
| POST | /verify-phone | Bearer | Verify phone OTP |
| POST | /resend-verification-email | Public | Resend email OTP (2min cooldown) |
| POST | /resend-verification-phone | Bearer | Resend phone OTP (2min cooldown) |

### Users `/api/v1/users`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | /me | Bearer | Get current user profile |

### Cars `/api/v1/car`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /add | Bearer | Add car + trigger async OCR verification |
| GET | /my-cars | Bearer | List all user's cars |
| GET | /{carId}/my-car | Bearer | Get specific car |
| PUT | /{carId}/resend-verification | Bearer | Resubmit Carte Grise image |
| DELETE | /{carId}/delete | Bearer | Delete car + cancel reservations |
| DELETE | /delete-all-cars | Bearer | Delete all user's cars |

### Stations `/api/v1/stations`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /add-station | Admin | Create station |
| GET | / | Bearer | List all stations |
| GET | /{id} | Bearer | Station details with ports |
| GET | /near-me | Bearer | Stations within radius (earthdistance) |
| DELETE | /{id} | Admin | Delete station + cascade |

### Ports `/api/v1/stations/{stationId}/ports`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | / | Admin | Add port to station |
| GET | / | Bearer | List ports with availability |
| GET | /{portId}/slots | Bearer | Available time slots for port + date + duration |

### Reservations `/api/v1/reservations`
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | / | Bearer | Create reservation + send OTP |
| POST | /confirm | Bearer | Confirm reservation with OTP |
| POST | /checkin | Bearer | NFC-based port check-in (planned-not yet implemented)
| DELETE | /{id} | Bearer | Cancel reservation |

---

## Getting Started

> **Demo Note:** Phone verification via Twilio SMS is paused in the live demo to preserve API credits.
> Phone verified status is set automatically on registration. The full SMS OTP flow is implemented
> and can be re-enabled by removing the default `phoneNumberVerifiedAt` assignment in the authService.java.
> Email verification via SMTP remains fully active.

### Running with Docker (Recommended)

**Prerequisites:** Docker and Docker Compose installed.

1. Clone the repository:
   ```bash
   git clone https://github.com/mahmoud-zammit-chatti/ReservationSystem.git
   cd ReservationSystem
   ```

2. Copy the example env file and fill in your values:
   ```bash
   cp .env.example .env
   ```

3. Start the full stack (Spring Boot + PostgreSQL):
   ```bash
   docker compose up --build
   ```

The app will be available at `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

> PostgreSQL data is persisted in a named Docker volume. The app waits for the database to be healthy before starting.

---

### Running without Docker

<details>
<summary>Manual setup instructions</summary>

**Prerequisites:** Java 21, PostgreSQL 14+, Maven

#### 1. Enable PostgreSQL Extensions

Connect to your database and run:

```sql
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
```

#### 2. Configure Environment Variables

Copy `.env.example` to `application-local.properties` under `src/main/resources/` and fill in your values:

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_NAME=your_db_name
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# JPA / Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Flyway
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
SPRING_FLYWAY_BASELINE_ON_MIGRATE=false
SPRING_FLYWAY_BASELINE_VERSION=1
SPRING_FLYWAY_VALIDATE_ON_MIGRATE=true

# JWT
JWT_SECRET=your_256bit_hex_secret
JWT_EXPIRATION=900000
REFRESH_TOKEN_SECRET=your_256bit_hex_secret
REFRESH_TOKEN_EXPIRATION_DAYS=30

# Twilio
TWILIO_ACCOUNT_SID=ACxxxxx
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1xxxxxxxxxx

# Spring Mail
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Azure Storage
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...
AZURE_CONTAINER_NAME=your-container-name

# Azure OCR
AZURE_DOC_INTELLIGENCE_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
AZURE_DOC_INTELLIGENCE_KEY=your_key

# Schedulers
CAR_SCHEDULER_INTERVAL=600000
PORT_SCHEDULER_INTERVAL=600000
```

> ⚠️ Never commit real credentials. Use environment variables or a gitignored `application-local.properties`.

#### 3. Run the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

</details>

---

## Testing Notes

- Unit and integration tests cover Spring context loading and OCR extraction logic (VIN/plate regex parsing)
- NFC check-in can be tested via Postman by manually sending the `accessIdentifier` value — the endpoint is hardware-agnostic
- OCR verification requires a real Azure Document Intelligence resource — mock the service for unit tests

---

## Roadmap

### Completed
- [x] JWT authentication with access + refresh token rotation
- [x] Two-factor account verification (email OTP + SMS via Twilio)
- [x] Role-based access control (`ROLE_USER`, `ROLE_ADMIN`)
- [x] User status gating (`INACTIVE → ACTIVE`)
- [x] Car onboarding with Azure Blob Storage upload
- [x] Async OCR verification via Azure Document Intelligence
- [x] VIN and Tunisian plate extraction with regex
- [x] Car blocking/unblocking with 24h scheduler
- [x] Station and port management (admin-secured)
- [x] Geospatial station discovery with earthdistance
- [x] Time-slot availability engine with overlap detection
- [x] Full reservation booking lifecycle
- [x] OTP-confirmed reservation creation
- [x] Automated background jobs (no-show detection, slot completion)
- [x] Docker + Docker Compose setup

### Planned
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] NFC-based port check-in
- [ ] Notification log (in-app, SMS, email history)
- [ ] Race condition protection on port reservations (pessimistic locking / Redis distributed locks)
- [ ] Billing and penalty enforcement
- [ ] Expanded test coverage (integration + e2e)
- [ ] Frontend integration (React + Leaflet map)

---

*Built by Mahmoud Zammit — Spring Boot Backend Project — 2026*