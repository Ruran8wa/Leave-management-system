package com.lms.leave.dto;

import com.lms.leave.entity.LeaveStatus;
import com.lms.leave.entity.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTOs for Leave requests/responses
 */
public class LeaveDto {
    
    @Data
    public static class CreateLeaveRequest {
        @NotNull(message = "User ID is required")
        private Long userId;
        
        @NotNull(message = "Start date is required")
        private LocalDate startDate;
        
        @NotNull(message = "End date is required")
        private LocalDate endDate;
        
        @NotNull(message = "Leave type is required")
        private LeaveType leaveType;
        
        private String reason;
        
        private String documentPath;  // Optional document upload path
    }
    
    @Data
    public static class ApprovalRequest {
        @NotNull(message = "Status is required")
        private LeaveStatus status; // APPROVED or REJECTED
        
        private String approvalComments;  // Manager's comments
        
        private String rejectionReason;
    }
    
    @Data
    public static class UpdateLeaveStatusRequest {
        @NotNull(message = "Status is required")
        private LeaveStatus status; // APPROVED or REJECTED
        
        private String approvalComments;
        
        private String rejectionReason;
    }
    
    @Data
    public static class LeaveResponse {
        private Long id;
        private Long userId;
        private String employeeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveType leaveType;
        private String reason;
        private LeaveStatus status;
        private Long approvedBy;
        private String approverName;
        private String approvalComments;
        private String rejectionReason;
        private Integer numberOfDays;
        private String documentPath;
        private String message;
    }
    
    @Data
    public static class LeaveBalanceResponse {
        private LeaveType leaveType;
        private String leaveTypeName;
        private Integer totalAllocated;
        private Integer used;
        private Integer pending;
        private Integer carriedOver;
        private Integer remaining;
        private Integer available;
        private LocalDate carryOverExpiryDate;
        private Integer accruedToDate;  // For PTO: days accrued up to current month
    }
    
    @Data
    public static class ColleagueOnLeaveResponse {
        private Long userId;
        private String employeeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private LeaveType leaveType;
    }
}
