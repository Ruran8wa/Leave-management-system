# LMS Frontend

A React-based frontend application for the Leave Management System (LMS).

## Features

- **Authentication**: Login and Registration with JWT
- **Dashboard**: View leave balance, recent requests, holidays, and colleagues on leave
- **Leave Management**: Apply for leave, view leave history with filtering
- **Manager Functions**: Approve/reject pending leave requests (Manager role)
- **Responsive Design**: Clean and modern UI

## Prerequisites

- Node.js (v16 or higher)
- npm or yarn

## Installation

```bash
# Install dependencies
npm install
```

## Configuration

The frontend is configured to connect to the backend API at `http://localhost:8080/api`. 

To change the API URL, edit the `API_BASE_URL` in `src/services/api.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Running the Application

```bash
# Development mode
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

The application will be available at `http://localhost:5173`

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── Layout.jsx      # Main layout with navigation
│   └── PrivateRoute.jsx # Protected route wrapper
├── context/            # React Context
│   └── AuthContext.jsx # Authentication context
├── pages/              # Page components
│   ├── Login.jsx       # Login page
│   ├── Register.jsx    # Registration page
│   ├── Dashboard.jsx   # Dashboard page
│   ├── MyLeaves.jsx    # Leave history page
│   ├── ApplyLeave.jsx  # Apply for leave page
│   └── ManagerPending.jsx # Manager approval page
├── services/           # API services
│   ├── api.js          # Axios configuration
│   ├── authService.js  # Authentication API calls
│   └── leaveService.js # Leave management API calls
├── App.jsx             # Main app component with routing
└── main.jsx            # Application entry point
```

## Available Routes

- `/login` - Login page
- `/register` - Registration page
- `/dashboard` - Dashboard (protected)
- `/leaves` - My leave requests (protected)
- `/apply-leave` - Apply for leave (protected)
- `/manager/pending` - Pending approvals (protected, Manager only)

## User Roles

- **STAFF**: Can view balance, apply for leave, and view their leave history
- **MANAGER**: All STAFF permissions + approve/reject team leave requests
- **ADMIN**: Full system access (future features)

## Technologies Used

- React 19
- React Router DOM - Routing
- Axios - HTTP client
- Vite - Build tool and dev server
- CSS3 - Styling

## Development

The application uses:
- JWT tokens stored in localStorage for authentication
- Axios interceptors for automatic token injection and error handling
- Context API for global state management
- React Router for client-side routing

## Building for Production

```bash
npm run build
```

The production build will be in the `dist/` directory.
