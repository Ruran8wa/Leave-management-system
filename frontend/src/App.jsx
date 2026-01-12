import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import MyLeaves from './pages/MyLeaves';
import ApplyLeave from './pages/ApplyLeave';
import ManagerPending from './pages/ManagerPending';
import AdminLeaveManagement from './pages/AdminLeaveManagement';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
          <Route
            path="/leaves"
            element={
              <PrivateRoute>
                <MyLeaves />
              </PrivateRoute>
            }
          />
          <Route
            path="/apply-leave"
            element={
              <PrivateRoute>
                <ApplyLeave />
              </PrivateRoute>
            }
          />
          <Route
            path="/manager/pending"
            element={
              <PrivateRoute requiredRole="MANAGER">
                <ManagerPending />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/leave-management"
            element={
              <PrivateRoute requiredRole="ADMIN">
                <AdminLeaveManagement />
              </PrivateRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
