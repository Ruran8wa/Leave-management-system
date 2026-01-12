import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { leaveService } from '../services/leaveService';
import Layout from '../components/Layout';
import './Auth.css';

const ApplyLeave = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    userId: user.id,
    leaveType: 'ANNUAL_LEAVE',
    startDate: '',
    endDate: '',
    reason: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (new Date(formData.startDate) > new Date(formData.endDate)) {
      setError('End date must be after start date');
      return;
    }

    setLoading(true);

    try {
      await leaveService.applyForLeave(formData);
      setSuccess('Leave request submitted successfully!');
      setTimeout(() => {
        navigate('/leaves');
      }, 2000);
    } catch (err) {
      console.error('Leave application error:', err.response?.data);
      setError(err.response?.data?.message || 'Failed to submit leave request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="apply-leave-container">
        <div className="auth-card">
          <h1>Apply for Leave</h1>
          <p className="auth-subtitle">Submit your leave request</p>
          
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}
          
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="leaveType">Leave Type</label>
              <select
                id="leaveType"
                name="leaveType"
                value={formData.leaveType}
                onChange={handleChange}
                required
              >
                <option value="ANNUAL_LEAVE">Annual Leave</option>
                <option value="SICK_LEAVE">Sick Leave</option>
                <option value="PERSONAL_TIME_OFF">Personal Time Off</option>
                <option value="COMPASSIONATE_LEAVE">Compassionate Leave</option>
                <option value="MATERNITY_LEAVE">Maternity Leave</option>
                <option value="PATERNITY_LEAVE">Paternity Leave</option>
                <option value="UNPAID_LEAVE">Unpaid Leave</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="startDate">Start Date</label>
              <input
                id="startDate"
                name="startDate"
                type="date"
                value={formData.startDate}
                onChange={handleChange}
                required
                min={new Date().toISOString().split('T')[0]}
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="endDate">End Date</label>
              <input
                id="endDate"
                name="endDate"
                type="date"
                value={formData.endDate}
                onChange={handleChange}
                required
                min={formData.startDate || new Date().toISOString().split('T')[0]}
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="reason">Reason</label>
              <textarea
                id="reason"
                name="reason"
                value={formData.reason}
                onChange={handleChange}
                required
                placeholder="Please provide a reason for your leave request"
                rows="4"
              />
            </div>
            
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Leave Request'}
            </button>
          </form>
        </div>
      </div>
    </Layout>
  );
};

export default ApplyLeave;
