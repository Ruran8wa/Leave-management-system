import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { leaveService } from '../services/leaveService';
import Layout from '../components/Layout';
import './MyLeaves.css';

const MyLeaves = () => {
  const { user } = useAuth();
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetchLeaves();
  }, []);

  const fetchLeaves = async () => {
    try {
      setLoading(true);
      const data = await leaveService.getMyLeaves(user.id);
      setLeaves(data);
    } catch (error) {
      console.error('Error fetching leaves:', error);
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

  const filteredLeaves = filter === 'ALL' 
    ? leaves 
    : leaves.filter(leave => leave.status === filter);

  if (loading) {
    return (
      <Layout>
        <div className="loading">Loading leaves...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="my-leaves">
        <div className="page-header">
          <h1>My Leave Requests</h1>
          <div className="filter-buttons">
            <button 
              className={filter === 'ALL' ? 'active' : ''} 
              onClick={() => setFilter('ALL')}
            >
              All
            </button>
            <button 
              className={filter === 'PENDING' ? 'active' : ''} 
              onClick={() => setFilter('PENDING')}
            >
              Pending
            </button>
            <button 
              className={filter === 'APPROVED' ? 'active' : ''} 
              onClick={() => setFilter('APPROVED')}
            >
              Approved
            </button>
            <button 
              className={filter === 'REJECTED' ? 'active' : ''} 
              onClick={() => setFilter('REJECTED')}
            >
              Rejected
            </button>
          </div>
        </div>

        {filteredLeaves.length > 0 ? (
          <div className="leaves-table-container">
            <table className="leaves-table">
              <thead>
                <tr>
                  <th>Leave Type</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Days</th>
                  <th>Status</th>
                  <th>Reason</th>
                  <th>Applied On</th>
                </tr>
              </thead>
              <tbody>
                {filteredLeaves.map((leave) => (
                  <tr key={leave.id}>
                    <td>{leave.leaveType}</td>
                    <td>{new Date(leave.startDate).toLocaleDateString()}</td>
                    <td>{new Date(leave.endDate).toLocaleDateString()}</td>
                    <td>{leave.numberOfDays}</td>
                    <td>
                      <span 
                        className="status-badge" 
                        style={{ backgroundColor: getStatusColor(leave.status) }}
                      >
                        {leave.status}
                      </span>
                    </td>
                    <td className="reason-cell">{leave.reason}</td>
                    <td>{new Date(leave.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="no-leaves">
            <p>No leave requests found</p>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default MyLeaves;
