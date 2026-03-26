# Spring Boot Hotel Booking System

A Spring Boot REST API for managing hotel rooms, users, and bookings.  
The project provides endpoints for room search, reservation flow, and booking administration.

---

## Features

- User registration and JWT authentication
- Room listing and room availability search
- Booking creation, retrieval, and lifecycle management
- Validation and centralized error handling with `ProblemDetail`
- Interactive API documentation with Swagger UI

---

## Tech Stack

- Java 17+
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Maven
- Database: PostgreSQL
- Liquibase
- Springdoc OpenAPI (Swagger UI)

---

## Prerequisites

Before running the project, ensure you have:

- JDK 17 or newer
- Maven 3.8+
- Git
- A running PostgreSQL server
- Proper credentials configured in a local `.env` file

---

## Installation / Setup

### 1) Clone the repository

```bash
git clone https://github.com/Sava2901/nttdata-task3.git
cd nttdata-task3
```

### 2) Configure environment variables

Copy the template and fill values:

```bash
cp .env.example .env
```

The application imports `.env` via `spring.config.import` and uses values like:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET_KEY`
- `JWT_EXPIRATION`
- `JWT_ISSUER`
- `JWT_AUDIENCE`

Main runtime config lives in:

- `backend/src/main/resources/application.yml`

### 3) Run the application

```bash
mvn clean spring-boot:run
```

The app starts on:

- `http://localhost:8081`

---

## API Documentation

This project uses Swagger/OpenAPI via Springdoc.

Once the application is running, open:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`


