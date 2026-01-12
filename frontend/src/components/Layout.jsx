import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="nav-brand">
          <Link to="/dashboard">LMS System</Link>
        </div>
        <div className="nav-links">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/leaves">My Leaves</Link>
          {user?.role !== 'ADMIN' && (
            <Link to="/apply-leave">Apply Leave</Link>
          )}
          {user?.role === 'MANAGER' && (
            <Link to="/manager/pending">Pending Approvals</Link>
          )}
          {user?.role === 'ADMIN' && (
            <Link to="/admin/leave-management">Leave Management</Link>
          )}
        </div>
        <div className="nav-user">
          <span className="user-name">{user?.firstName} {user?.lastName}</span>
          <span className="user-role">({user?.role})</span>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </nav>
      <main className="main-content">{children}</main>
    </div>
  );
};

export default Layout;
