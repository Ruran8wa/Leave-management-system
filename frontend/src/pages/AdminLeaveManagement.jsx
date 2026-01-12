import { useState, useEffect } from 'react';
import { leaveService } from '../services/leaveService';
import { useAuth } from '../context/AuthContext';
import Layout from '../components/Layout';
import './MyLeaves.css';

const AdminLeaveManagement = () => {
  const [pendingLeaves, setPendingLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const { user } = useAuth();

  useEffect(() => {
    fetchPendingStaffLeaves();
  }, []);

  const fetchPendingStaffLeaves = async () => {
    try {
      setLoading(true);
      const data = await leaveService.getPendingStaffLeaves();
      setPendingLeaves(data);
    } catch (error) {
      console.error('Error fetching pending staff leaves:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (leaveId) => {
    try {
      setActionLoading(leaveId);
      await leaveService.adminApproveLeave(leaveId, user.id, 'Approved by admin');
      await fetchPendingStaffLeaves();
      alert('Leave request approved successfully');
    } catch (error) {
      console.error('Error approving leave:', error);
      alert('Failed to approve leave request: ' + (error.response?.data?.message || error.message));
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (leaveId) => {
    const comments = prompt('Please provide a reason for rejection:');
    if (!comments) return;

    try {
      setActionLoading(leaveId);
      await leaveService.adminRejectLeave(leaveId, user.id, comments);
      await fetchPendingStaffLeaves();
      alert('Leave request rejected successfully');
    } catch (error) {
      console.error('Error rejecting leave:', error);
      alert('Failed to reject leave request: ' + (error.response?.data?.message || error.message));
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="loading">Loading pending staff leave requests...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="my-leaves">
        <div className="page-header">
          <h1>Staff Leave Management</h1>
          <p className="subtitle">Approve or reject leave requests from staff members</p>
        </div>

        {pendingLeaves.length > 0 ? (
          <div className="leaves-table-container">
            <table className="leaves-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Leave Type</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Days</th>
                  <th>Reason</th>
                  <th>Applied On</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {pendingLeaves.map((leave) => (
                  <tr key={leave.id}>
                    <td>{leave.userName}</td>
                    <td>{leave.leaveType}</td>
                    <td>{new Date(leave.startDate).toLocaleDateString()}</td>
                    <td>{new Date(leave.endDate).toLocaleDateString()}</td>
                    <td>{leave.numberOfDays}</td>
                    <td className="reason-cell">{leave.reason}</td>
                    <td>{new Date(leave.createdAt).toLocaleDateString()}</td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn-approve"
                          onClick={() => handleApprove(leave.id)}
                          disabled={actionLoading === leave.id}
                        >
                          Approve
                        </button>
                        <button
                          className="btn-reject"
                          onClick={() => handleReject(leave.id)}
                          disabled={actionLoading === leave.id}
                        >
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="no-leaves">
            <p>No pending staff leave requests</p>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default AdminLeaveManagement;
