## Plan: 24h Car Unblock Flow

Use a **single periodic scheduler**, not per-car timers. With your current design (`status`, `verificationAttempts`, `corruptedAt`), the safest approach is: enforce blocking immediately in request flow, and run a batch scheduler to clean up expired blocks. A **10-minute cycle** is a good default (low DB load, max 10-minute delay if no user action). This keeps behavior predictable and scalable.

### Steps
1. Add blocked-window checks in [CarVerificationService](src/main/java/com/reservationSys/reservationSys/Services/Car/CarVerificationService.java) before incrementing attempts or launching OCR.
2. Extend [CarRepo](src/main/java/com/reservationSys/reservationSys/Repositories/CarRepo.java) with a query for `CarStatus.BLOCKED` where `corruptedAt <= now - 24h`.
3. Create a scheduled batch service in `Services/Car` to reset expired cars to `UNVERIFIED`, clear `corruptedAt`, and reset `verificationAttempts`.
4. Enable scheduling in [ReservationSysApplication.java](src/main/java/com/reservationSys/reservationSys/ReservationSysApplication.java) and set scheduler interval via `application.properties`.

### Further Considerations
1. Preferred scheduler cycle? Option A: 5 min (faster unlock), Option B: 10 min (balanced), Option C: 60 min (lowest DB load).
2. Should unblock happen only by scheduler, or also instantly on user request when 24h already passed?
3. On unblock, should attempts reset to `0` (recommended) or keep historical count for analytics?
