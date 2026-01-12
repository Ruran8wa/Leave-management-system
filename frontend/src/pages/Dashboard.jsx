import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { leaveService } from '../services/leaveService';
import Layout from '../components/Layout';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [balances, setBalances] = useState([]);
  const [recentLeaves, setRecentLeaves] = useState([]);
  const [holidays, setHolidays] = useState([]);
  const [colleagues, setColleagues] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [balanceData, leavesData, holidaysData, colleaguesData] = await Promise.all([
        leaveService.getLeaveBalance(user.id),
        leaveService.getMyLeaves(user.id),
        leaveService.getPublicHolidays(),
        leaveService.getColleaguesOnLeave(),
      ]);
      
      setBalances(balanceData);
      setRecentLeaves(leavesData.slice(0, 5));
      setHolidays(holidaysData.slice(0, 5));
      setColleagues(colleaguesData);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED': return '#27ae60';
      case 'REJECTED': return '#e74c3c';
      case 'PENDING': return '#f39c12';
      case 'CANCELLED': return '#95a5a6';
      default: return '#34495e';
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="loading">Loading dashboard...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="dashboard">
        <h1>Welcome, {user?.firstName} {user?.lastName}!</h1>
        
        <div className="dashboard-grid">
          {/* Leave Balance Cards */}
          <div className="card">
            <h2>Leave Balance</h2>
            <div className="balance-list">
              {balances.length > 0 ? (
                balances.map((balance, index) => (
                  <div key={index} className="balance-item">
                    <span className="balance-type">{balance.leaveType}</span>
                    <div className="balance-details">
                      <span className="balance-remaining">{balance.remaining} days</span>
                      <span className="balance-total">of {balance.totalAllowed}</span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="no-data">No balance data available</p>
              )}
            </div>
          </div>

          {/* Recent Leave Requests */}
          <div className="card">
            <h2>Recent Leave Requests</h2>
            <div className="leave-list">
              {recentLeaves.length > 0 ? (
                recentLeaves.map((leave) => (
                  <div key={leave.id} className="leave-item">
                    <div className="leave-dates">
                      {new Date(leave.startDate).toLocaleDateString()} - {new Date(leave.endDate).toLocaleDateString()}
                    </div>
                    <div className="leave-type">{leave.leaveType}</div>
                    <span 
                      className="leave-status" 
                      style={{ backgroundColor: getStatusColor(leave.status) }}
                    >
                      {leave.status}
                    </span>
                  </div>
                ))
              ) : (
                <p className="no-data">No leave requests yet</p>
              )}
            </div>
          </div>

          {/* Upcoming Holidays */}
          <div className="card">
            <h2>Upcoming Holidays</h2>
            <div className="holiday-list">
              {holidays.length > 0 ? (
                holidays.map((holiday) => (
                  <div key={holiday.id} className="holiday-item">
                    <div className="holiday-date">
                      {new Date(holiday.date).toLocaleDateString()}
                    </div>
                    <div className="holiday-name">{holiday.name}</div>
                  </div>
                ))
              ) : (
                <p className="no-data">No upcoming holidays</p>
              )}
            </div>
          </div>

          {/* Colleagues on Leave */}
          <div className="card">
            <h2>Colleagues on Leave</h2>
            <div className="colleagues-list">
              {colleagues.length > 0 ? (
                colleagues.map((colleague, index) => (
                  <div key={index} className="colleague-item">
                    <div className="colleague-name">{colleague.userName}</div>
                    <div className="colleague-dates">
                      {new Date(colleague.startDate).toLocaleDateString()} - {new Date(colleague.endDate).toLocaleDateString()}
                    </div>
                  </div>
                ))
              ) : (
                <p className="no-data">No colleagues on leave</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
