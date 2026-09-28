EV Station Finder & Slot Booking - Backend

Project Overview

EV Station Finder & Slot Booking is a Spring Boot backend application for managing electric vehicle charging stations and charging-slot bookings.

The backend provides REST APIs for:
• User registration and login
• JWT-based authentication
• Role-based authorization
• Charging station management
• Charger management
• Connector management
• Nearby charging station search
• EV charging slot booking
• Advance payment confirmation
• Payment expiry handling
• Booking status and payment status management
• Gujarat BEE charging-station dataset import

Technology Stack

Java: 17
Spring Boot: 4.1.0
Spring Data JPA: Database persistence
Hibernate: ORM
MySQL: Relational database
Spring Security: Authentication and Authorization
JJWT: 0.12.6
Maven: Build and dependency management
Lombok: Reduce boilerplate code
Apache Commons CSV: CSV dataset processing
Postman: API testing

System Architecture

The backend follows a layered architecture:

Client / Postman / Frontend
            |
            v
     Controller Layer
            |
            v
       Service Layer
            |
            v
     Repository Layer
            |
            v
      JPA / Hibernate
            |
            v
          MySQL

Architecture Layers

Controller Layer
Handles HTTP requests and exposes REST APIs.

Service Layer
Contains application and business logic.

Repository Layer
Handles database operations using Spring Data JPA.

Entity Layer
Represents the database tables and their relationships.

DTO Layer
Used for transferring request and response data between the client and backend.

Security Layer
Handles JWT-based authentication and request authentication.

Exception Layer
Provides centralized exception handling.

Project Structure

src/main/java/com/example/Ev_Station_Backend

├── config
│   └── SecurityConfig.java
├── Controller
│   ├── AuthController.java
│   ├── BEEImportController.java
│   ├── BookingController.java
│   ├── ChargerController.java
│   ├── ChargingStationController.java
│   ├── ConnectorController.java
│   ├── TestController.java
│   └── UserController.java
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
├── entity
│   ├── Booking.java
│   ├── Charger.java
│   ├── ChargerPricing.java
│   ├── ChargingStation.java
│   ├── Connector.java
│   └── User.java
├── Enum
│   ├── BookingStatus.java
│   └── PaymentStatus.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── Repository
│   ├── BookingRepository.java
│   ├── ChargerPricingRepository.java
│   ├── ChargerRepository.java
│   ├── ChargingStationRepository.java
│   ├── ConnectorRepository.java
│   └── UserRepository.java
├── Security
│   └── JwtAuthenticationFilter.java
└── Service
    ├── AuthService.java
    ├── BEEImportService.java
    ├── BookingService.java
    ├── ChargerService.java
    ├── ChargingStationService.java
    ├── ConnectorService.java
    ├── JwtService.java
    └── UserService.java

Main Features

1. User Authentication

The backend supports:
• User registration
• User login
• JWT token generation
• JWT authentication
• Role-based authorization

Authentication APIs:
POST /api/auth/register
POST /api/auth/login

2. Charging Station Management

Charging stations contain information such as:
• CPO name
• Government / Private classification
• State
• District
• City / Village
• Location
• Latitude
• Longitude
• Source
• Google Place ID
• Status
• Created time
• Updated time

3. Charger Management

Each charging station can contain multiple chargers.

A charger stores:
• Charger type
• Charger rating
• Connector rating
• Number of connectors
• BEE source index
• Associated charging station

Relationship:
Charging Station → Charger (1:N)

4. Connector Management

Connectors belong to chargers.

Each connector contains:
• Connector number
• Status
• Associated charger

Relationship:
Charger → Connector (1:N)

A unique constraint is applied to:
charger_id + connector_number

This prevents duplicate connector numbers for the same charger.

5. Charger Pricing

The project maintains pricing based on charger type.

The ChargerPricing entity contains:
• Charger type
• Price per kWh

Each charger type has a unique pricing entry.

6. Nearby Charging Station Search

The backend supports searching for charging stations using:
• Latitude
• Longitude
• Radius

Example:
GET /api/stations/nearby?latitude=23.0225&longitude=72.5714&radius=10

The API returns charging stations available within the requested search radius, subject to the backend's configured radius limit.

7. BEE Dataset Import

The project uses a Gujarat EV charging-station dataset from BEE.

Dataset location:
src/main/resources/data/Gujarat_EV_Charging_Stations_BEE_Cleaned.csv

The backend provides an API to import the BEE dataset:
POST /api/import/bee

The Charger entity contains a unique beeSourceIndex which identifies the source row from the BEE CSV during import.

8. Booking System

Users can create charging-slot bookings for a connector.

A booking contains:
• User
• Connector
• Start time
• End time
• Estimated amount
• Advance amount
• Final amount
• Booking status
• Payment status
• Payment expiry time
• Cancellation charge
• Refund amount
• Created time
• Cancelled time

Booking creation API:
POST /api/bookings

9. Advance Payment

The booking system supports an advance-payment flow.

Basic flow:

Create Booking
      ↓
Calculate Estimated Amount
      ↓
Calculate Advance Amount
      ↓
Payment Pending
      ↓
Confirm Payment
      ↓
Booking Confirmation

The booking stores:
• estimatedAmount
• advanceAmount
• finalAmount
• paymentStatus
• paymentExpiresAt

Payment confirmation API:
POST /api/bookings/{bookingId}/confirm-payment

10. Booking Status

The project supports:
• PENDING
• CONFIRMED
• CANCELLED
• COMPLETED
• EXPIRED

11. Payment Status

The project supports:
• PENDING
• PAID
• FAILED
• REFUNDED
• PARTIALLY_REFUNDED

12. Database Entities

The main database entities are:
• User
• ChargingStation
• Charger
• Connector
• ChargerPricing
• Booking

Entity Relationships:

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

User
 |
 | 1 : N
 v
Booking

API Documentation

Authentication APIs

POST /api/auth/register
Description: Register a new user.

POST /api/auth/login
Description: Login and receive authentication token.

Charging Station APIs

POST /api/stations
Description: Create charging station.

GET /api/stations
Description: Get all charging stations.

GET /api/stations/{id}
Description: Get charging station by ID.

GET /api/stations/nearby
Description: Search nearby charging stations.

DELETE /api/stations/{id}
Description: Delete charging station.

Charger APIs

POST /api/chargers
Description: Create charger.

GET /api/chargers
Description: Get all chargers.

GET /api/chargers/{id}
Description: Get charger by ID.

DELETE /api/chargers/{id}
Description: Delete charger.

Connector APIs

POST /api/connectors
Description: Create connector.

GET /api/connectors
Description: Get all connectors.

GET /api/connectors/{id}
Description: Get connector by ID.

DELETE /api/connectors/{id}
Description: Delete connector.

Booking APIs

POST /api/bookings
Description: Create booking.

POST /api/bookings/{bookingId}/confirm-payment
Description: Confirm advance payment.

User APIs

POST /api/users
Description: Create user.

GET /api/users
Description: Get all users.

GET /api/users/{id}
Description: Get user by ID.

DELETE /api/users/{id}
Description: Delete user.

BEE Dataset API

POST /api/import/bee
Description: Import BEE charging-station dataset.

Test API

GET /api/test
Description: Test backend endpoint.

Database Configuration

The project uses MySQL.

Create the database:

CREATE DATABASE ev_station;

Configure the database connection in:
src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/ev_station
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

Do not commit actual database passwords, JWT secrets, or API keys to GitHub.

Prerequisites

Before running the project, install:
• Java 17
• MySQL
• Git

The project includes Maven Wrapper, so Maven does not need to be installed separately.

Check Java:
java -version

How to Run

1. Clone the Repository

git clone <YOUR_GITHUB_REPOSITORY_URL>

2. Open the Project

cd Ev_Station_Backend

3. Configure MySQL

Create the database:

CREATE DATABASE ev_station;

Update the database username and password in:
src/main/resources/application.properties

4. Run the Application

Windows:
.\mvnw.cmd spring-boot:run

Linux / macOS:
./mvnw spring-boot:run

Build the Project

Windows:
.\mvnw.cmd clean package

Linux / macOS:
./mvnw clean package

API Testing with Postman

Recommended testing flow:

1. Register User
2. Login
3. Receive JWT Token
4. Use JWT Token for protected APIs
5. Find Charging Station
6. Select Charger
7. Select Connector
8. Create Booking
9. Confirm Advance Payment

Security

The backend uses:

Spring Security
      +
JWT Authentication
      +
JwtAuthenticationFilter

JWT is used to authenticate requests.

Role-based authorization is configured through:
SecurityConfig.java

Exception Handling

The project provides centralized exception handling using:
GlobalExceptionHandler.java

Resource-specific errors are handled using:
ResourceNotFoundException.java

Git Development Modules

The project has been developed incrementally using feature-based Git commits.

Major development areas include:

SQL / Spring Boot Setup
        ↓
JWT Authentication
        ↓
Global Exception Handling
        ↓
Role-Based Authorization
        ↓
Charging Station & Charger
        ↓
BEE Dataset Import
        ↓
Nearby Station Search
        ↓
Connector Management
        ↓
Booking & Availability
        ↓
Advance Payment & Payment Expiry

Future Scope

Possible future enhancements include:
• Frontend / Android application integration
• Real online payment gateway integration
• Real-time charger availability
• Improved booking management
• User profile enhancements
• Charging-session tracking
• Additional station data integrations

Project Status

Current backend modules include:
• User Authentication
• JWT Authorization
• Role-Based Authorization
• Charging Stations
• Chargers
• Connectors
• Charger Pricing
• BEE Dataset Import
• Nearby Station Search
• Booking
• Advance Payment
• Payment Expiry
• Exception Handling

Author

MCA SEM-3 Project

EV Station Finder & Slot Booking

