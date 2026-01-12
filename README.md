# LMS Full Stack - Leave Management System

A full-stack Leave Management System with Spring Boot backend and React frontend.

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 16+

### 1. Start Backend (with Database)
```bash
cd backend
docker-compose up
```
Backend runs at: `http://localhost:8080`

### 2. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs at: `http://localhost:5173`

### 3. Access the Application
- Open `http://localhost:5173`
- Register a new account
- Login and start using the system

## Default Roles
- **STAFF**: Apply for leave, view balance, see colleagues on leave
- **MANAGER**: Approve/reject team leave requests (+ all STAFF features)
- **ADMIN**: Manage holidays, generate reports (+ all features)

## API Documentation
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Tech Stack
**Backend:** Java 17, Spring Boot, PostgreSQL, JWT  
**Frontend:** React 19, Vite, React Router

## Project Structure
```
LMS-Full/
├── backend/          # Spring Boot API
└── frontend/         # React UI
```

## Troubleshooting

**Backend won't start:**
- Ensure Docker is running
- Check if port 8080 is available

**Frontend can't connect:**
- Verify backend is running at `http://localhost:8080`
- Clear browser cache/localStorage

**CORS errors:**
- Backend allows `http://localhost:5173` by default
- Change port in `SecurityConfig.java` if needed

## Building for Production

**Backend:**
```bash
cd backend
mvn clean package
java -jar target/lms-application.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Serve dist/ folder
```