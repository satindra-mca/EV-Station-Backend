# EV Station Finder & Slot Booking - Backend
## 📌 Project Overview
EV Station Finder & Slot Booking is a Spring Boot backend application for managing electric vehicle charging stations and charging-slot bookings.

The backend provides REST APIs for:

* User registration and login

* JWT-based authentication

* Role-based authorization

* Charging station management

* Charger management

* Connector management

* Charger pricing management

* Nearby charging station search

* EV charging slot booking

* Booking availability validation

* Advance payment confirmation

* Payment expiry handling

* Booking cancellation

* Cancellation charge calculation

* Refund calculation

* Booking status and payment status management

* Gujarat BEE charging-station dataset import

* Centralized exception handling
* Owner account creation and station assignment
* Owner charger and connector management
* Owner booking dashboard (all / today / tomorrow)
* Booking history and booking detail APIs

---
## 🛠️ Technology Stack
| Technology         | Version / Purpose              |

| ------------------ | ------------------------------ |

| Java               | 17                             |

| Spring Boot        | 4.1.0                          |

| Spring Data JPA    | Database persistence           |

| Hibernate          | ORM                            |

| MySQL              | Relational Database            |

| Spring Security    | Authentication & Authorization |

| JJWT               | 0.12.6                         |

| Maven              | Build & Dependency Management  |

| Lombok             | Reduce Boilerplate Code        |

| Apache Commons CSV | CSV Dataset Processing         |

| Postman            | API Testing                    |

---
## 🏗️ System Architecture
```text

                    Client / Frontend / Postman

                              |

                              v

                    +-------------------+

                    | Controller Layer  |

                    +-------------------+

                              |

                              v

                    +-------------------+

                    |  Service Layer    |

                    +-------------------+

                              |

                              v

                    +-------------------+

                    | Repository Layer  |

                    +-------------------+

                              |

                              v

                    +-------------------+

                    | JPA / Hibernate   |

                    +-------------------+

                              |

                              v

                    +-------------------+

                    |      MySQL        |

                    +-------------------+

```

### Architecture Layers
**Controller Layer**

Handles HTTP requests and exposes REST APIs.

**Service Layer**

Contains application and business logic.

**Repository Layer**

Handles database operations using Spring Data JPA.

**Entity Layer**

Represents database tables and their relationships.

**DTO Layer**

Used for transferring request and response data between the client and backend.

**Security Layer**

Handles JWT-based authentication and request authentication.

**Exception Layer**

Provides centralized exception handling for application errors.

---
# 📂 Project Structure
```text
src/main/java/com/example/Ev_Station_Backend/
├── config/
│   └── SecurityConfig.java
├── Controller/
│   ├── admin/       (AdminOwnerController, BEEImportController)
│   ├── auth/        (AuthController)
│   ├── booking/     (BookingController)
│   ├── charger/     (ChargerController)
│   ├── connector/   (ConnectorController)
│   ├── owner/       (OwnerController, OwnerBookingController,
│   │                 OwnerChargerController, OwnerConnectorController)
│   ├── station/     (ChargingStationController)
│   └── user/        (UserController)
├── Service/
│   ├── admin/  auth/  booking/  charger/  connector/
│   └── owner/  station/  user/
├── dto/
│   ├── admin/  auth/  booking/  charger/  connector/
│   └── owner/  station/  user/
├── Repository/
├── entity/
├── Enum/
├── Security/
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── BookingException.java
    └── ResourceNotFoundException.java
```

The backend was reorganized into feature-based packages in commit `0d453bd`. The old `TestController` is no longer part of the current source layout.

---
# 🚀 Main Features
## 1. User Authentication
The backend supports:

* User registration

* User login

* JWT token generation

* JWT authentication

* Role-based authorization

### Authentication APIs
```http

POST /api/auth/register

POST /api/auth/login

```

---
## 2. Charging Station Management
Charging stations contain information such as:

* CPO Name

* Government / Private classification

* State

* District

* City / Village

* Location

* Latitude

* Longitude

* Source

* Google Place ID

* Status

* Created Time

* Updated Time

---
## 3. Charger Management
Each charging station can contain multiple chargers.

A charger stores:

* Charger Type

* Charger Rating

* Connector Rating

* Number of Connectors

* BEE Source Index

* Associated Charging Station

### Relationship
```text

Charging Station

       |

       | 1 : N

       v

    Charger

```

---
## 4. Connector Management
Connectors belong to chargers.

Each connector contains:

* Connector Number

* Status

* Associated Charger

### Relationship
```text

Charger

   |

   | 1 : N

   v

Connector

```

A unique constraint is applied to:

```text

charger_id + connector_number

```

This prevents duplicate connector numbers for the same charger.

---
## 5. Charger Pricing
The project maintains pricing based on charger type.

The `ChargerPricing` entity contains:

* Charger Type

* Price Per kWh

Each charger type has a unique pricing entry.

The booking service uses the configured charger pricing to calculate the estimated booking amount.

---
## 6. Nearby Charging Station Search
The backend supports searching charging stations using:

* Latitude

* Longitude

* Radius

### Example
```http

GET /api/stations/nearby?latitude=23.0225&longitude=72.5714&radius=10

```

The API returns charging stations available within the requested search radius.

---
## 7. BEE Dataset Import
The project uses a Gujarat EV charging-station dataset from BEE. The import endpoint requires the `ADMIN` role.

### Dataset Location
```text

src/main/resources/data/Gujarat_EV_Charging_Stations_BEE_Cleaned.csv

```

### Import API
```http

POST /api/import/bee

```

The `Charger` entity contains a unique:

```text

beeSourceIndex

```

This identifies the source row from the BEE CSV during import and helps prevent duplicate imports.

---
## 8. Booking System
Users can create charging-slot bookings for a connector.

A booking contains:

* User

* Connector

* Start Time

* End Time

* Estimated Amount

* Advance Amount

* Final Amount

* Booking Status

* Payment Status

* Payment Expiry Time

* Cancellation Charge

* Refund Amount

* Created Time

* Cancelled Time

### Booking API
```http

POST /api/bookings

```

### Booking Availability
The booking system checks whether the selected connector is already booked for the requested time.

A transition buffer is also applied between bookings to reduce scheduling conflicts.

---
## 9. Advance Payment
The booking system supports an advance-payment flow to reduce unpaid or fake bookings.

### Booking Flow
```text

Create Booking

      |

      v

Calculate Estimated Amount

      |

      v

Calculate Advance Amount

      |

      v

Payment Pending

      |

      +----------------------+

      |                      |

      | Payment Timeout      | Payment Successful

      v                      v

   EXPIRED               CONFIRMED

```

The booking stores:

```text

estimatedAmount

advanceAmount

finalAmount

paymentStatus

paymentExpiresAt

```

### Payment Confirmation API
```http

POST /api/bookings/{bookingId}/payment

```

The payment request contains the advance payment amount.

When the correct advance payment is confirmed:

```text

PaymentStatus = PAID

BookingStatus = CONFIRMED

paymentExpiresAt = null

```

If the payment is not completed before the payment expiry time, the booking can expire.

---
## 10. Booking Cancellation
The booking system supports cancellation of bookings.

### Cancellation API
```http

POST /api/bookings/{bookingId}/cancel

```

Cancellation processing can update:

* Booking Status

* Payment Status

* Cancellation Charge

* Refund Amount

* Cancellation Time

### Cancellation Flow
```text

Booking

   |

   v

Check Booking Status

   |

   v

Check Payment Status

   |

   v

Calculate Cancellation Charge

   |

   v

Calculate Refund Amount

   |

   v

Update Booking

   |

   v

CANCELLED

```

For paid bookings, the cancellation logic considers the remaining time before the booking starts to determine the applicable cancellation charge and refund amount (see the detailed rules in Section 16).

For pending unpaid bookings, cancellation does not require a refund because no advance payment has been completed.

---
## 11. Booking Status
The project supports the following booking statuses:

```text

PENDING

CONFIRMED

CANCELLED

COMPLETED

EXPIRED

```

### Status Meaning
| Status    | Description                                             |

| --------- | ------------------------------------------------------- |

| PENDING   | Booking created but advance payment is pending          |

| CONFIRMED | Advance payment has been successfully confirmed         |

| CANCELLED | Booking has been cancelled                              |

| COMPLETED | Booking has been completed                              |

| EXPIRED   | Booking expired before successful payment or completion |

---
## 12. Payment Status
The project supports the following payment statuses:

```text

PENDING

PAID

FAILED

REFUNDED

PARTIALLY_REFUNDED

CANCELLED

```

### Status Meaning
| Status             | Description                                                                                     |

| ------------------ | ----------------------------------------------------------------------------------------------- |

| PENDING            | Payment has not yet been completed                                                              |

| PAID               | Advance payment was successfully confirmed                                                      |

| FAILED             | Payment attempt was not successful                                                              |

| REFUNDED           | Payment amount was fully refunded                                                               |

| PARTIALLY_REFUNDED | Part of the payment amount was refunded                                                         |

| CANCELLED          | Payment was cancelled because the associated booking was cancelled or payment was not completed |

---
## 13. Owner Management

The `OWNER` role manages the charging station assigned by an administrator.

* View the assigned station
* View all station bookings, today's bookings, and tomorrow's bookings
* Create, view, update, and delete chargers at the assigned station
* Create, view, update, and delete connectors belonging to that station
* Access checks prevent an owner from managing another owner's station resources

### Owner Workflow

```text
ADMIN creates OWNER account
          |
          v
ADMIN assigns a charging station
          |
          v
OWNER logs in with JWT
          |
          v
View station / Manage chargers and connectors / View bookings
```

An owner without an assigned station cannot use station-specific management features. The current assignment policy limits an owner to one station.

---

## 14. Admin and Role-Based Access

The backend has three roles:

| Role | Responsibilities |
| --- | --- |
| `USER` | Search stations, view chargers/connectors, create and manage own bookings |
| `OWNER` | Manage chargers/connectors and view bookings for the assigned station |
| `ADMIN` | Create owner accounts, assign stations, manage administrative resources, import BEE data |

Registration and login are public. Other routes require a valid JWT. `/api/admin/**` and `/api/users/**` require `ADMIN`; `/api/owner/**` requires `OWNER`. Administrative station/charger/connector changes and BEE import also require `ADMIN`.

---

## 15. Booking History and Booking Details

Users can retrieve their own bookings and inspect an individual booking.

```http
GET /api/bookings/my
GET /api/bookings/{bookingId}
```

Booking history includes the station, charger, connector, scheduled time, amounts, booking/payment statuses, and cancellation/refund information. Booking detail access checks ownership.

---

## 16. Booking Validation and Payment Rules

The booking service validates that the start time is in the future, the end time follows the start time, and the connector is `AVAILABLE`. It locks the connector during creation and checks conflicts with confirmed bookings and unexpired pending bookings.

* **Transition buffer:** 15 minutes between conflicting bookings
* **Estimated energy:** 5 kWh per booked hour
* **Estimated amount:** estimated energy multiplied by configured charger-type price per kWh
* **Advance amount:** 20% of estimated amount
* **Payment expiry:** 15 minutes after creating a pending booking
* **Payment integration:** simulated confirmation only; no external gateway or actual transfer

### Cancellation and Refund Rules

| Time before booking start | Charge on paid advance | Refund of paid advance |
| --- | --- | --- |
| At least 8 hours | 0% | 100% |
| 5 to less than 8 hours | 20% | 80% |
| Less than 5 hours, before start | 50% | 50% |

Unpaid pending bookings can be cancelled without a refund. These are application-level calculations, not real financial refunds.

---

# 🗄️ Database Entities
Main entities:

* User

* ChargingStation

* Charger

* Connector

* ChargerPricing

* Booking

### Entity Relationships
```text

                    ChargingStation

                           |

                           | 1 : N

                           v

                        Charger

                           |

                           | 1 : N

                           v

                       Connector

                           |

                           | 1 : N

                           v

                        Booking

```

### User and Booking
```text

User

 |

 | 1 : N

 v

Booking

```

---
# 🔌 API Documentation
## Authentication APIs
| Method | Endpoint             | Description                            |

| ------ | -------------------- | -------------------------------------- |

| POST   | `/api/auth/register` | Register a new user                    |

| POST   | `/api/auth/login`    | Login and receive authentication token |

## Charging Station APIs
| Method | Endpoint               | Description                     |

| ------ | ---------------------- | ------------------------------- |

| POST   | `/api/stations`        | Create charging station         |

| GET    | `/api/stations`        | Get all charging stations       |

| GET    | `/api/stations/{id}`   | Get charging station by ID      |

| GET    | `/api/stations/nearby` | Search nearby charging stations |

| DELETE | `/api/stations/{id}`   | Delete charging station         |

## Charger APIs
| Method | Endpoint             | Description       |

| ------ | -------------------- | ----------------- |

| POST   | `/api/chargers`      | Create charger    |

| GET    | `/api/chargers`      | Get all chargers  |

| GET    | `/api/chargers/{id}` | Get charger by ID |

| DELETE | `/api/chargers/{id}` | Delete charger    |

## Connector APIs
| Method | Endpoint               | Description         |

| ------ | ---------------------- | ------------------- |

| POST   | `/api/connectors`      | Create connector    |

| GET    | `/api/connectors`      | Get all connectors  |

| GET    | `/api/connectors/{id}` | Get connector by ID |

| DELETE | `/api/connectors/{id}` | Delete connector    |

## Booking APIs
| Method | Endpoint                            | Description             |

| ------ | ----------------------------------- | ----------------------- |

| POST   | `/api/bookings`                     | Create booking          |

| POST   | `/api/bookings/{bookingId}/payment` | Confirm advance payment |

| POST   | `/api/bookings/{bookingId}/cancel`  | Cancel booking          |
| GET    | `/api/bookings/my`                 | View own booking history |
| GET    | `/api/bookings/{bookingId}`        | View own booking details |

## Owner Station and Booking APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/owner/station` | View assigned charging station |
| GET | `/api/owner/bookings` | View all station bookings |
| GET | `/api/owner/bookings/today` | View today's bookings |
| GET | `/api/owner/bookings/tomorrow` | View tomorrow's bookings |

## Owner Charger APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/owner/chargers` | List own station chargers |
| GET | `/api/owner/chargers/{id}` | Get charger details |
| POST | `/api/owner/chargers` | Add charger |
| PUT | `/api/owner/chargers/{id}` | Update charger |
| DELETE | `/api/owner/chargers/{id}` | Delete charger if allowed |

## Owner Connector APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/owner/connectors` | List own station connectors |
| GET | `/api/owner/connectors/{id}` | Get connector details |
| POST | `/api/owner/connectors` | Add connector |
| PUT | `/api/owner/connectors/{id}` | Update connector |
| DELETE | `/api/owner/connectors/{id}` | Delete connector if allowed |

## Admin Owner APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/admin/owners` | Create OWNER account |
| PUT | `/api/admin/owners/{ownerId}/station/{stationId}` | Assign a station to an OWNER |

## User APIs
| Method | Endpoint          | Description    |

| ------ | ----------------- | -------------- |

| POST   | `/api/users`      | Create user    |

| GET    | `/api/users`      | Get all users  |

| GET    | `/api/users/{id}` | Get user by ID |

| DELETE | `/api/users/{id}` | Delete user    |

## BEE Dataset API
| Method | Endpoint          | Description                         |

| ------ | ----------------- | ----------------------------------- |

| POST   | `/api/import/bee` | Import BEE charging-station dataset |


---
# 🗃️ Database Configuration
The project uses MySQL.

### Create Database
```sql

CREATE DATABASE ev_station;

```

Database configuration file:

```text

src/main/resources/application.properties

```

Example:

```properties

spring.datasource.url=jdbc:mysql://localhost:3306/ev_station

spring.datasource.username=YOUR_USERNAME

spring.datasource.password=YOUR_PASSWORD

```

> ****Security:**** Do not commit actual database passwords, JWT secrets, or API keys to GitHub.

---
# ⚙️ Prerequisites
Install the following:

* Java 17

* MySQL

* Git

The project includes Maven Wrapper, so Maven does not need to be installed separately.

### Check Java
```bash

java -version

```

---
# ▶️ How to Run
## 1. Clone Repository
```bash

git clone https://github.com/satindra-mca/EV-Station-Backend.git

```

## 2. Open Project
```bash

cd EV-Station-Backend

```

## 3. Configure MySQL
Create the database:

```sql

CREATE DATABASE ev_station;

```

Update the database username and password in:

```text

src/main/resources/application.properties

```

## 4. Run Application
### Windows
```powershell

.\mvnw.cmd spring-boot:run

```

### Linux / macOS
```bash

./mvnw spring-boot:run

```

---
# 🔨 Build Project
### Windows
```powershell

.\mvnw.cmd clean package

```

### Linux / macOS
```bash

./mvnw clean package

```

---
# 🧪 API Testing with Postman
Recommended testing flow:

```text

1. Register User

       |

       v

2. Login

       |

       v

3. Receive JWT Token

       |

       v

4. Use JWT Token for Protected APIs

       |

       v

5. Find Charging Station

       |

       v

6. Select Charger

       |

       v

7. Select Connector

       |

       v

8. Create Booking

       |

       v

9. Confirm Advance Payment

       |

       v

10. Booking Confirmed

       |

       +---- Cancel Booking ----> Refund / Cancellation Charge

```

---
# 🔐 Security
The backend uses:

```text

Spring Security

       +

JWT Authentication

       +

JwtAuthenticationFilter

```

JWT is used to authenticate requests.

Role-based authorization is configured through:

```text

SecurityConfig.java

```

Protected APIs require a valid JWT token where configured by the security rules. An invalid/missing token generally results in HTTP 401; insufficient permissions result in HTTP 403.

---
# ⚠️ Exception Handling
The project provides centralized exception handling using:

```text

GlobalExceptionHandler.java

```

Resource-specific errors are handled using:

```text

ResourceNotFoundException.java

```

Booking-specific business errors are handled using:

```text

BookingException.java

```

This allows the application to return structured error responses instead of exposing raw exceptions.

---
# 🌿 Git Development Modules
The project has been developed incrementally using feature-based Git commits.

```text

SQL / Spring Boot Setup

        |

        v

JWT Authentication

        |

        v

Global Exception Handling

        |

        v

Role-Based Authorization

        |

        v

Charging Station & Charger

        |

        v

BEE Dataset Import

        |

        v

Nearby Station Search

        |

        v

Connector Management

        |

        v

Booking & Availability

        |

        v

Advance Payment & Payment Expiry

        |

        v

Payment & Booking Cancellation

```

---
# 🔮 Future Scope
Possible future enhancements:

* Frontend / Android application integration

* Real online payment gateway integration

* Real-time charger availability

* Improved booking management

* User profile enhancements

* Charging-session tracking

* Additional station data integrations

* Notification system for booking and payment events

* Admin dashboard

* Charging-session billing based on actual energy consumption
* Comprehensive automated integration and security tests
* Consistent pricing between charger records and the `ChargerPricing` table

---
# 📌 Project Status
Current backend modules include:

* User Authentication

* JWT Authorization

* Role-Based Authorization

* Charging Stations

* Chargers

* Connectors

* Charger Pricing

* BEE Dataset Import

* Nearby Station Search

* Booking

* Booking Availability Validation

* Advance Payment

* Payment Expiry

* Booking Cancellation

* Cancellation Charge

* Refund Calculation

* Exception Handling

---
# 👨‍💻 Author
**MCA SEM-3 Project**

**EV Station Finder & Slot Booking**
