# LMS Full Stack - Quick Start Guide

## Overview
This is a Leave Management System (LMS) with a Java Spring Boot backend and React frontend.

## Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+
- PostgreSQL (or use Docker Compose)

## Backend Setup

### 1. Configure Database
```bash
cd backend
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Edit `application-local.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lms_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Using Docker (Recommended)
```bash
cd backend
./docker-start.sh  # Starts PostgreSQL in Docker
```

### 3. Run Backend
```bash
cd backend
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### API Documentation
Once running, access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

## Frontend Setup

### 1. Install Dependencies
```bash
cd frontend
npm install
```

### 2. Run Frontend
```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

## Testing the Application

### 1. Register a New User
- Navigate to `http://localhost:5173`
- Click "Register here"
- Fill in the registration form
- Submit

### 2. Login
- Use your registered credentials to login
- You'll be redirected to the dashboard

### 3. Explore Features

**As STAFF (default role):**
- View leave balance on dashboard
- Apply for leave
- View leave history
- See colleagues on leave
- View public holidays

**As MANAGER:**
- All STAFF features
- Approve/reject team leave requests

**As ADMIN:**
- Full system access
- Manage public holidays
- Generate reports

## Project Structure

```
LMS-Full/
├── backend/              # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lms/
│   │   │   │   ├── auth/         # Authentication module
│   │   │   │   ├── leave/        # Leave management module
│   │   │   │   ├── config/       # Security & config
│   │   │   │   └── common/       # Shared components
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
└── frontend/             # React frontend
    ├── src/
    │   ├── components/   # Reusable components
    │   ├── context/      # React Context (Auth)
    │   ├── pages/        # Page components
    │   ├── services/     # API services
    │   ├── App.jsx       # Main app
    │   └── main.jsx      # Entry point
    └── package.json
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/2fa/setup` - Setup 2FA

### Employee Leave
- `POST /api/employee/leaves/apply` - Apply for leave
- `GET /api/employee/leaves/my-leaves/{userId}` - View my leaves
- `GET /api/employee/leaves/balance/{userId}` - View leave balance
- `GET /api/employee/leaves/colleagues-on-leave` - View colleagues on leave
- `GET /api/employee/leaves/public-holidays` - View holidays

### Manager
- `GET /api/manager/leaves/pending` - View pending approvals
- `PATCH /api/manager/leaves/{leaveId}/approve` - Approve leave
- `PATCH /api/manager/leaves/{leaveId}/reject` - Reject leave

### Admin
- `POST /api/admin/holidays` - Add public holiday
- `GET /api/admin/holidays` - View all holidays
- `DELETE /api/admin/holidays/{id}` - Delete holiday

## Technologies Used

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security with JWT
- PostgreSQL
- Hibernate/JPA
- Swagger/OpenAPI

### Frontend
- React 19
- React Router DOM
- Axios
- Vite
- CSS3

## Common Issues

### Backend won't start
- Check if PostgreSQL is running: `docker ps` or `psql`
- Verify database credentials in `application-local.properties`
- Ensure port 8080 is not in use

### Frontend can't connect to backend
- Verify backend is running at `http://localhost:8080`
- Check CORS settings in backend `SecurityConfig.java`
- Check API_BASE_URL in `frontend/src/services/api.js`

### CORS Errors
The backend is configured to allow requests from `http://localhost:5173`. If you change the frontend port, update `SecurityConfig.java`:

```java
.allowedOrigins("http://localhost:5173", "http://localhost:YOUR_PORT")
```

## Development Tips

1. **Hot Reload**: Both frontend and backend support hot reload during development
2. **API Testing**: Use Swagger UI for testing backend APIs
3. **State Management**: Frontend uses Context API for global state
4. **Authentication**: JWT tokens are stored in localStorage

## Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/lms-application.jar
```

### Frontend
```bash
cd frontend
npm run build
# Serve the dist/ folder with your preferred web server
```

## License
MIT
