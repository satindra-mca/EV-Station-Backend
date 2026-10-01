# EV Station Finder & Slot Booking - Backend

## 📌 Project Overview

EV Station Finder & Slot Booking is a Spring Boot backend application for managing electric vehicle charging stations and charging-slot bookings.

The backend provides REST APIs for:

- User registration and login
- JWT-based authentication
- Role-based authorization
- Charging station management
- Charger management
- Connector management
- Nearby charging station search
- EV charging slot booking
- Advance payment confirmation
- Payment expiry handling
- Booking status and payment status management
- Gujarat BEE charging-station dataset import

---

## 🛠️ Technology Stack

| Technology | Version / Purpose |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.0 |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| MySQL | Relational Database |
| Spring Security | Authentication & Authorization |
| JJWT | 0.12.6 |
| Maven | Build & Dependency Management |
| Lombok | Reduce Boilerplate Code |
| Apache Commons CSV | CSV Dataset Processing |
| Postman | API Testing |

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
Used for transferring request and response data between client and backend.

**Security Layer**
Handles JWT-based authentication and request authentication.

**Exception Layer**
Provides centralized exception handling.

---

# 📂 Project Structure

```text
src/main/java/com/example/Ev_Station_Backend

├── config
│   └── SecurityConfig.java
│
├── Controller
│   ├── AuthController.java
│   ├── BEEImportController.java
│   ├── BookingController.java
│   ├── ChargerController.java
│   ├── ChargingStationController.java
│   ├── ConnectorController.java
│   ├── TestController.java
│   └── UserController.java
│
├── dto
│   ├── BookingRequest.java
│   ├── BookingResponse.java
│   ├── ChargerRequest.java
│   ├── ChargerResponse.java
│   ├── ChargingStationRequest.java
│   ├── ChargingStationResponse.java
│   ├── ConnectorRequest.java
│   ├── ConnectorResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   └── RegisterResponse.java
│
├── entity
│   ├── Booking.java
│   ├── Charger.java
│   ├── ChargerPricing.java
│   ├── ChargingStation.java
│   ├── Connector.java
│   └── User.java
│
├── Enum
│   ├── BookingStatus.java
│   └── PaymentStatus.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
│
├── Repository
│   ├── BookingRepository.java
│   ├── ChargerPricingRepository.java
│   ├── ChargerRepository.java
│   ├── ChargingStationRepository.java
│   ├── ConnectorRepository.java
│   └── UserRepository.java
│
├── Security
│   └── JwtAuthenticationFilter.java
│
└── Service
    ├── AuthService.java
    ├── BEEImportService.java
    ├── BookingService.java
    ├── ChargerService.java
    ├── ChargingStationService.java
    ├── ConnectorService.java
    ├── JwtService.java
    └── UserService.java
```

---

# 🚀 Main Features

## 1. User Authentication

The backend supports:

- User registration
- User login
- JWT token generation
- JWT authentication
- Role-based authorization

### Authentication APIs

```http
POST /api/auth/register
POST /api/auth/login
```

---

## 2. Charging Station Management

Charging stations contain:

- CPO Name
- Government / Private classification
- State
- District
- City / Village
- Location
- Latitude
- Longitude
- Source
- Google Place ID
- Status
- Created Time
- Updated Time

---

## 3. Charger Management

Each charging station can contain multiple chargers.

A charger stores:

- Charger Type
- Charger Rating
- Connector Rating
- Number of Connectors
- BEE Source Index
- Associated Charging Station

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

- Connector Number
- Status
- Associated Charger

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

- Charger Type
- Price Per kWh

Each charger type has a unique pricing entry.

---

## 6. Nearby Charging Station Search

The backend supports searching charging stations using:

- Latitude
- Longitude
- Radius

### Example

```http
GET /api/stations/nearby?latitude=23.0225&longitude=72.5714&radius=10
```

The API returns charging stations available within the requested search radius.

---

## 7. BEE Dataset Import

The project uses a Gujarat EV charging-station dataset from BEE.

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

This identifies the source row from the BEE CSV during import.

---

## 8. Booking System

Users can create charging-slot bookings for a connector.

A booking contains:

- User
- Connector
- Start Time
- End Time
- Estimated Amount
- Advance Amount
- Final Amount
- Booking Status
- Payment Status
- Payment Expiry Time
- Cancellation Charge
- Refund Amount
- Created Time
- Cancelled Time

### Booking API

```http
POST /api/bookings
```

---

## 9. Advance Payment

The booking system supports an advance-payment flow.

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
      v
Confirm Payment
      |
      v
Booking Confirmation
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
POST /api/bookings/{bookingId}/confirm-payment
```

---

## 10. Booking Status

The project supports:

```text
PENDING
CONFIRMED
CANCELLED
COMPLETED
EXPIRED
```

---

## 11. Payment Status

The project supports:

```text
PENDING
PAID
FAILED
REFUNDED
PARTIALLY_REFUNDED
```

---

# 🗄️ Database Entities

Main entities:

- User
- ChargingStation
- Charger
- Connector
- ChargerPricing
- Booking

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

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive authentication token |

## Charging Station APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/stations` | Create charging station |
| GET | `/api/stations` | Get all charging stations |
| GET | `/api/stations/{id}` | Get charging station by ID |
| GET | `/api/stations/nearby` | Search nearby charging stations |
| DELETE | `/api/stations/{id}` | Delete charging station |

## Charger APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/chargers` | Create charger |
| GET | `/api/chargers` | Get all chargers |
| GET | `/api/chargers/{id}` | Get charger by ID |
| DELETE | `/api/chargers/{id}` | Delete charger |

## Connector APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/connectors` | Create connector |
| GET | `/api/connectors` | Get all connectors |
| GET | `/api/connectors/{id}` | Get connector by ID |
| DELETE | `/api/connectors/{id}` | Delete connector |

## Booking APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/bookings` | Create booking |
| POST | `/api/bookings/{bookingId}/confirm-payment` | Confirm advance payment |

## User APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create user |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| DELETE | `/api/users/{id}` | Delete user |

## BEE Dataset API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/import/bee` | Import BEE charging-station dataset |

## Test API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/test` | Test backend endpoint |

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

> **Security:** Do not commit actual database passwords, JWT secrets, or API keys to GitHub.

---

# ⚙️ Prerequisites

Install the following:

- Java 17
- MySQL
- Git

The project includes Maven Wrapper, so Maven does not need to be installed separately.

### Check Java

```bash
java -version
```

---

# ▶️ How to Run

## 1. Clone Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

## 2. Open Project

```bash
cd Ev_Station_Backend
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
```

---

# 🔮 Future Scope

Possible future enhancements:

- Frontend / Android application integration
- Real online payment gateway integration
- Real-time charger availability
- Improved booking management
- User profile enhancements
- Charging-session tracking
- Additional station data integrations

---

# 📌 Project Status

Current backend modules include:

- User Authentication
- JWT Authorization
- Role-Based Authorization
- Charging Stations
- Chargers
- Connectors
- Charger Pricing
- BEE Dataset Import
- Nearby Station Search
- Booking
- Advance Payment
- Payment Expiry
- Exception Handling

---

# 👨‍💻 Author

**MCA SEM-3 Project**

**EV Station Finder & Slot Booking**
