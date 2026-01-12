import api from './api';

export const leaveService = {
  // Employee endpoints
  applyForLeave: async (leaveData) => {
    const response = await api.post('/employee/leaves/apply', leaveData);
    return response.data;
  },

  getMyLeaves: async (userId) => {
    const response = await api.get(`/employee/leaves/my-leaves/${userId}`);
    return response.data;
  },

  getLeaveBalance: async (userId, year) => {
    const url = year 
      ? `/employee/leaves/balance/${userId}/${year}`
      : `/employee/leaves/balance/${userId}`;
    const response = await api.get(url);
    return response.data;
  },

  getLeaveDetails: async (leaveId) => {
    const response = await api.get(`/employee/leaves/${leaveId}`);
    return response.data;
  },

  getColleaguesOnLeave: async () => {
    const response = await api.get('/employee/leaves/colleagues-on-leave');
    return response.data;
  },

  getPublicHolidays: async () => {
    const response = await api.get('/employee/leaves/public-holidays');
    return response.data;
  },

  // Manager endpoints
  getPendingLeaves: async () => {
    const response = await api.get('/manager/leaves/pending');
    return response.data;
  },

  getAllTeamLeaves: async () => {
    const response = await api.get('/manager/leaves/all');
    return response.data;
  },

  // Admin - Get all leave requests
  getAllLeaves: async () => {
    const response = await api.get('/manager/leaves/all');
    return response.data;
  },
  
  // Admin - Get pending staff leave requests only
  getPendingStaffLeaves: async () => {
    const response = await api.get('/admin/leaves/pending-staff');
    return response.data;
  },

  approveLeave: async (leaveId, managerId, comments) => {
    const response = await api.patch(
      `/manager/leaves/${leaveId}/approve?managerId=${managerId}`,
      { 
        status: 'APPROVED',
        approvalComments: comments 
      }
    );
    return response.data;
  },

  rejectLeave: async (leaveId, managerId, comments) => {
    const response = await api.patch(
      `/manager/leaves/${leaveId}/approve?managerId=${managerId}`,
      { 
        status: 'REJECTED',
        rejectionReason: comments 
      }
    );
    return response.data;
  },
  
  // Admin endpoints for approving/rejecting staff leaves
  adminApproveLeave: async (leaveId, adminId, comments) => {
    const response = await api.patch(
      `/admin/leaves/${leaveId}/approve?adminId=${adminId}`,
      { 
        status: 'APPROVED',
        approvalComments: comments 
      }
    );
    return response.data;
  },

  adminRejectLeave: async (leaveId, adminId, comments) => {
    const response = await api.patch(
      `/admin/leaves/${leaveId}/reject?adminId=${adminId}`,
      { 
        status: 'REJECTED',
        rejectionReason: comments 
      }
    );
    return response.data;
  },

  // Admin endpoints
  addPublicHoliday: async (holidayData) => {
    const response = await api.post('/admin/holidays', holidayData);
    return response.data;
  },

  getAllHolidays: async () => {
    const response = await api.get('/admin/holidays');
    return response.data;
  },

  deleteHoliday: async (id) => {
    const response = await api.delete(`/admin/holidays/${id}`);
    return response.data;
  },
};
