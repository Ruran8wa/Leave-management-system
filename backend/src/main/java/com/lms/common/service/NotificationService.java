package com.lms.common.service;

import com.lms.auth.entity.User;
import com.lms.leave.entity.Leave;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Notification Service - Sends email notifications
 * 
 * This is a placeholder implementation.
 * In production, integrate with:
 * - Spring Mail (SMTP)
 * - SendGrid
 * - AWS SES
 * - Twilio SendGrid
 */
@Slf4j
@Service
public class NotificationService {
    
    /**
     * Send notification when leave is approved
     */
    public void sendLeaveApprovedNotification(User user, Leave leave, String approverName) {
        log.info("EMAIL: Sending leave approved notification to {} for leave ID {}", 
                user.getEmail(), leave.getId());
        
        // TODO: Implement actual email sending
        String subject = "Leave Request Approved";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your leave request from %s to %s has been approved by %s.\n\n" +
                "Leave Type: %s\n" +
                "Days: %d\n" +
                "Comments: %s\n\n" +
                "Best regards,\n" +
                "LMS Team",
                user.getFirstName(),
                leave.getStartDate(),
                leave.getEndDate(),
                approverName,
                leave.getLeaveType().getDisplayName(),
                leave.getNumberOfDays(),
                leave.getApprovalComments() != null ? leave.getApprovalComments() : "None"
        );
        
        log.info("EMAIL CONTENT:\nTo: {}\nSubject: {}\nBody: {}", user.getEmail(), subject, body);
    }
    
    /**
     * Send notification when leave is rejected
     */
    public void sendLeaveRejectedNotification(User user, Leave leave, String approverName) {
        log.info("EMAIL: Sending leave rejected notification to {} for leave ID {}", 
                user.getEmail(), leave.getId());
        
        String subject = "Leave Request Rejected";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your leave request from %s to %s has been rejected by %s.\n\n" +
                "Leave Type: %s\n" +
                "Reason: %s\n\n" +
                "Best regards,\n" +
                "LMS Team",
                user.getFirstName(),
                leave.getStartDate(),
                leave.getEndDate(),
                approverName,
                leave.getLeaveType().getDisplayName(),
                leave.getRejectionReason() != null ? leave.getRejectionReason() : "Not provided"
        );
        
        log.info("EMAIL CONTENT:\nTo: {}\nSubject: {}\nBody: {}", user.getEmail(), subject, body);
    }
    
    /**
     * Send notification to manager when new leave is requested
     */
    public void sendLeaveRequestNotification(User manager, User employee, Leave leave) {
        log.info("EMAIL: Sending new leave request notification to manager {} for employee {}", 
                manager.getEmail(), employee.getEmail());
        
        String subject = "New Leave Request - Pending Approval";
        String body = String.format(
                "Dear %s,\n\n" +
                "%s %s has requested leave:\n\n" +
                "From: %s\n" +
                "To: %s\n" +
                "Type: %s\n" +
                "Days: %d\n" +
                "Reason: %s\n\n" +
                "Please review and approve/reject this request.\n\n" +
                "Best regards,\n" +
                "LMS Team",
                manager.getFirstName(),
                employee.getFirstName(),
                employee.getLastName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getLeaveType().getDisplayName(),
                leave.getNumberOfDays(),
                leave.getReason() != null ? leave.getReason() : "Not provided"
        );
        
        log.info("EMAIL CONTENT:\nTo: {}\nSubject: {}\nBody: {}", manager.getEmail(), subject, body);
    }
}
