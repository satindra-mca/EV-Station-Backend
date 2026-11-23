# EV Station Finder & Slot Booking â€” Backend

**MCA Semester 3 project** | Spring Boot REST API | Android frontend integration in progress

Repository: https://github.com/satindra-mca/EV-Station-Backend
Branch: `main` | Documentation baseline: commit `0d453bd` (23 November 2026)
## Overview

This backend supports EV charging-station discovery and connector-based slot bookings. It provides JWT authentication, role-based access for `USER`, `OWNER`, and `ADMIN`, station/charger/connector management, nearby search, owner dashboards, and a simulated advance-payment and cancellation workflow. The Android application (Java/XML) is a separate frontend development phase.

**Project status:** Backend MVP checkpoint; not production-ready. The payment endpoint confirms an amount in application logic; it does **not** charge a card, call a payment gateway, or transfer refunds.

## Technology

- Java 17, Spring Boot 4.1.0, Spring Security, JWT (JJWT), Spring Data JPA/Hibernate
- MySQL (`ev_station`), Maven Wrapper, Lombok, Apache Commons CSV
- Postman for manual API verification; Git/GitHub for version control

## Architecture and source layout

```text
Android client / Postman
        |
Spring Security + JWT filter
        |
Controller -> Service -> Repository -> JPA/Hibernate -> MySQL
                   |
              DTO / Entity / Enum / Exception
```

Main Java package: `com.example.Ev_Station_Backend` (retain actual case).

```text
src/main/java/com/example/Ev_Station_Backend/
â”œâ”€â”€ Controller/
â”‚   â”œâ”€â”€ admin/      (AdminOwnerController, BEEImportController)
â”‚   â”œâ”€â”€ auth/       (AuthController)
â”‚   â”œâ”€â”€ booking/    (BookingController)
â”‚   â”œâ”€â”€ charger/    (ChargerController)
â”‚   â”œâ”€â”€ connector/  (ConnectorController)
â”‚   â”œâ”€â”€ owner/      (OwnerController, OwnerBookingController,
â”‚   â”‚                OwnerChargerController, OwnerConnectorController)
â”‚   â”œâ”€â”€ station/    (ChargingStationController)
â”‚   â””â”€â”€ user/       (UserController)
â”œâ”€â”€ Service/
â”‚   â”œâ”€â”€ admin/ auth/ booking/ charger/ connector/
â”‚   â””â”€â”€ owner/ station/ user/
â”œâ”€â”€ dto/
â”‚   â”œâ”€â”€ admin/ auth/ booking/ charger/ connector/
â”‚   â””â”€â”€ owner/ station/ user/
â”œâ”€â”€ Repository/
â”œâ”€â”€ entity/
â”œâ”€â”€ Enum/
â”œâ”€â”€ Security/       (JwtAuthenticationFilter, JwtService)
â”œâ”€â”€ config/         (SecurityConfig)
â””â”€â”€ exception/      (GlobalExceptionHandler and domain exceptions)
```

`TestController` and the old flat Controller/Service/DTO package layout were removed or reorganized in `0d453bd`.

## Authentication and authorization

`POST /api/auth/register` and `POST /api/auth/login` are public. All other mapped endpoints require authentication under the current `SecurityConfig`.

Use the login response token in protected requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

| Route group | Access in current security configuration |
| --- | --- |
| `/api/auth/register`, `/api/auth/login` | Public |
| `/api/admin/**`, `/api/users/**` | `ADMIN` |
| `/api/owner/**` | `OWNER` |
| `POST`, `PUT`, `PATCH`, `DELETE` under `/api/stations/**`, `/api/chargers/**`, `/api/connectors/**` | `ADMIN` (where corresponding endpoint exists) |
| `POST /api/import/bee` | `ADMIN` |
| Station/charger/connector GET APIs and `/api/bookings/**` | Authenticated; booking service also checks booking ownership for relevant actions |

The JWT filter authenticates valid tokens; stateless sessions are configured. Unauthorized requests return 401; authenticated requests lacking the required role return 403. `ADMIN` is not automatically an `OWNER` for `/api/owner/**` under the present rules.

### Authentication request examples

```http
POST /api/auth/register
Content-Type: application/json

{"name":"Example User","email":"user@example.com","mobile":"9999999999","password":"example-password"}
```

```http
POST /api/auth/login
Content-Type: application/json

{"email":"user@example.com","password":"example-password"}
```

Login response fields: `message`, `token`, `userId`, `name`, `email`, `role`. Registration response includes `id`, `name`, `email`, `mobile`, `role`. Invalid credentials return 401; duplicate registration email returns 409.

## API reference

The following routes were checked against the controllers at commit `0d453bd`. `{id}` denotes a path parameter. `GET` endpoints are not necessarily publicly accessible: see the security table above.

### Authentication

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Authenticate and receive JWT |

### Charging stations

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/stations` | Create station (ADMIN) |
| GET | `/api/stations` | List stations |
| GET | `/api/stations/{id}` | Station detail |
| GET | `/api/stations/nearby?latitude=21.1702&longitude=72.8311&radius=10` | Stations within radius in km |
| DELETE | `/api/stations/{id}` | Delete station (ADMIN) |

Nearby search validates latitude (-90 to 90), longitude (-180 to 180), and radius (>0, up to 100 km). **The current controller does not expose a PUT station endpoint.**

### Chargers and connectors (administrative endpoints)

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/chargers` | Create charger |
| GET | `/api/chargers` | List chargers |
| GET | `/api/chargers/{id}` | Charger detail |
| DELETE | `/api/chargers/{id}` | Delete charger |
| POST | `/api/connectors` | Create connector |
| GET | `/api/connectors` | List connectors |
| GET | `/api/connectors/{id}` | Connector detail |
| DELETE | `/api/connectors/{id}` | Delete connector |

Mutating endpoints in these two groups require `ADMIN`. No PUT endpoints are defined in these two controllers at the documented commit; owner-specific updates use `/api/owner/**`.

### Booking (authenticated)

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/bookings` | Create a booking |
| POST | `/api/bookings/{bookingId}/payment` | Confirm simulated advance payment |
| POST | `/api/bookings/{bookingId}/cancel` | Cancel a booking |
| GET | `/api/bookings/my` | Logged-in user's booking history |
| GET | `/api/bookings/{bookingId}` | Booking details, subject to ownership check |

Create booking:

```json
{"connectorId":1,"startTime":"2026-12-01T10:00:00","endTime":"2026-12-01T11:00:00"}
```

Payment confirmation body: `{"amount":15.00}`. The value must match the `advanceAmount` returned for **that** booking; `15.00` is illustrative, not a fixed price. Cancellation body: `{"reason":"Change of plans"}`. Booking dates must be in the future when submitted.

Booking creation returns fields including `id`, `userId`, `connectorId`, `startTime`, `endTime`, `estimatedAmount`, `advanceAmount`, `finalAmount`, `status`, `paymentStatus`, `paymentExpiresAt`, `cancellationCharge`, `refundAmount`, `createdAt`, `cancelledAt`.

### Owner (OWNER role, assigned station required)

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/owner/station` | Assigned station summary |
| GET | `/api/owner/bookings` | All bookings at owner's station |
| GET | `/api/owner/bookings/today` | Today's bookings |
| GET | `/api/owner/bookings/tomorrow` | Tomorrow's bookings |
| GET | `/api/owner/chargers` | Owner station chargers |
| GET | `/api/owner/chargers/{id}` | Owner charger detail |
| POST | `/api/owner/chargers` | Add charger |
| PUT | `/api/owner/chargers/{id}` | Update charger |
| DELETE | `/api/owner/chargers/{id}` | Delete charger, subject to booking references |
| GET | `/api/owner/connectors` | Owner station connectors |
| GET | `/api/owner/connectors/{id}` | Owner connector detail |
| POST | `/api/owner/connectors` | Add connector |
| PUT | `/api/owner/connectors/{id}` | Update connector |
| DELETE | `/api/owner/connectors/{id}` | Delete connector, subject to booking references |

Owner operations check station ownership. The owner charger request DTO currently has `chargerType`, `chargerRating`, `connectorRating`, `noOfConnector`, and `chargingStationId`; the service derives the actual station from the logged-in owner rather than trusting the supplied station ID. Connector request fields: `connectorNumber`, `status`, `chargerId`.

### Admin

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/admin/owners` | Create owner account |
| PUT | `/api/admin/owners/{ownerId}/station/{stationId}` | Assign station to owner |
| POST | `/api/users` | Create user |
| GET | `/api/users` | List users |
| GET | `/api/users/{id}` | Get user |
| DELETE | `/api/users/{id}` | Delete user |
| POST | `/api/import/bee` | Import BEE charging-station CSV |

The BEE import controller is under `/api/import`, not `/api/admin`, but its POST route is explicitly ADMIN-only in `SecurityConfig`.

## Booking business rules

These rules are derived from the `BookingService` implementation at the documented commit.

- A booking must start in the future, end after it starts, and use a connector with status `AVAILABLE`.
- The service locks the connector row during creation and checks conflicts with existing `PENDING` (not expired) and `CONFIRMED` bookings.
- A **15-minute transition buffer** is enforced around blocking bookings.
- Estimated energy uses **5 kWh per booked hour**; the price is looked up in `ChargerPricing` by charger type. Estimated amount = estimated energy Ã— configured price per kWh.
- Advance payment is **20%** of estimated amount. A new booking starts as `PENDING` and has a **15-minute payment confirmation window**.
- The payment confirmation API checks booking ownership, status, expiry, and exact advance amount; a valid confirmation sets booking `CONFIRMED` and payment `PAID`.
- Pending unpaid cancellation has no refund. Paid cancellations before the booking start use the following advance-payment refund rules:

| Time remaining before start | Cancellation charge on advance | Refund of advance |
| --- | --- | --- |
| At least 8 hours | 0% | 100% |
| At least 5 but less than 8 hours | 20% | 80% |
| Less than 5 hours, before start | 50% | 50% |

Booking statuses: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `EXPIRED`. Payment statuses: `PENDING`, `PAID`, `FAILED`, `REFUNDED`, `PARTIALLY_REFUNDED`, `CANCELLED`.

**Important:** Payment and refunds are internal status/amount calculations, not external financial transactions. A booking `COMPLETED` enum exists, but do not assume a complete charging-session completion workflow exists.

## Database and data import

Database: MySQL `ev_station`.

```sql
CREATE DATABASE IF NOT EXISTS ev_station;
```

Main entities: `User`, `ChargingStation`, `Charger`, `Connector`, `Booking`, `ChargerPricing`.

```text
ChargingStation 1 â”€â”€ * Charger 1 â”€â”€ * Connector 1 â”€â”€ * Booking
User            1 â”€â”€ * Booking
ChargingStation â”€â”€ Owner (User with OWNER role, assignment constraints)
```

A charger can have multiple numbered connectors; `(charger_id, connector_number)` is unique. BEE CSV import uses `beeSourceIndex` to track source rows.

Dataset path (verify the file exists in the checked-out repository before import):

```text
src/main/resources/data/Gujarat_EV_Charging_Stations_BEE_Cleaned.csv
```

## Setup and run

Requirements: Java 17, MySQL, Git, and network access for Maven dependencies. Maven Wrapper is included.

```powershell
git clone https://github.com/satindra-mca/EV-Station-Backend.git
cd EV-Station-Backend
java -version
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Linux/macOS: replace `mvnw.cmd` with `./mvnw`.

Configure `src/main/resources/application.properties` or your environment with the correct datasource settings. Example placeholders:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ev_station
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Check the actual application's JWT secret and token configuration requirements before launching. **Never commit real passwords, JWT signing secrets, bearer tokens, or Google Maps keys.** Use local secrets/environment-specific configuration for development.

Build a package:

```powershell
.\mvnw.cmd clean package
```

## Testing and Android integration

Manual Postman scenarios previously checked include login, duplicate registration, JWT authorization, owner access controls, and booking/payment/cancellation cases. At the November 2026 checkpoint, `mvnw clean test` passed **one Spring context test**. This is **not** comprehensive automated integration-test coverage.

For Android Studio:

- Emulator-to-host development base URL: `http://10.0.2.2:8080/` (assuming backend runs on host port 8080).
- Physical device: use a reachable LAN address and ensure network/firewall access; do not use `localhost` to refer to the development computer from the phone.
- Development-only cleartext HTTP requires appropriate Android network security configuration; deployed apps should use HTTPS.
- Use `Authorization: Bearer <token>` for protected endpoints and handle 401/403 and booking validation errors in the UI.
- Treat the backend controllers and DTOs as the authoritative API contract. There is no dedicated availability-calendar endpoint documented here: the booking service checks conflicts on booking creation.

## Known limitations and follow-up work

1. **No real payment gateway:** advance confirmation/refunds are application-level simulations.
2. **Pricing consistency needs review:** booking calculations use the `ChargerPricing` table, while owner-created chargers currently set their separate `pricePerKwh` field to zero. Do not present owner charger pricing as authoritative booking pricing without reconciling these.
3. **No comprehensive automated API suite:** add controller/security/integration tests for permissions, concurrency, payment expiry, and refunds.
4. **No guaranteed live hardware availability:** connector status and booking schedules are database/application values, not verified live charging-station telemetry.
5. **Frontend in progress:** Android Java/XML UI, Google Maps, GPS, and end-to-end device tests remain separate deliverables.
6. **Configuration/security hardening:** review secrets handling, production HTTPS, logging, validation, and database migration strategy before deployment.

## Development history

- `cb1c2c3` â€” owner booking dashboard
- `6a081c8` â€” booking validation and concurrency improvements
- `0d453bd` â€” modular package organization and API security hardening

## License and attribution

MCA Semester-3 academic project. Check repository license and BEE dataset attribution/usage terms before public redistribution; this README does not assert a license that has not been verified.
