# Leave Management System

A Spring Boot application for managing employee leave requests with JWT authentication, role-based access control, and Swagger API documentation.

## Quick Start

### Prerequisites
- Java 21 LTS
- Maven 3.6+
- Docker & Docker Compose

### 1. Start PostgreSQL Database

```bash
./docker-start.sh
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

### 3. Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceTest
```

**Access Points:**
- Application: http://localhost:8080
- Swagger API Docs: http://localhost:8080/swagger-ui/index.html

## Using the API

### 1. Register a User

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STAFF"
}
```

### 2. Login

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response includes JWT token:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com"
}
```

### 3. Make Authenticated Requests

Add the token to request headers:
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Database Management

```bash
# Stop PostgreSQL
./docker-stop.sh

# Connect to PostgreSQL
docker exec -it lms-postgres psql -U lmsuser -d lmsdb

# View tables
\dt

# View users
SELECT * FROM users;

# Exit
\q
```

## Build for Production

```bash
mvn clean package
java -jar target/lms-application-1.0.0.jar
```

## Tech Stack

- Spring Boot 3.4.2
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 16
- Swagger/OpenAPI
- Docker Compose
- JUnit 5 + Mockito (Testing)

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: STAFF, MANAGER, ADMIN roles
- **Password Hashing**: BCrypt encryption
- **2FA Support**: Google Authenticator integration
- **OAuth2**: Google login integration
- **Secure Registration**: New users always registered as STAFF (prevents privilege escalation)
