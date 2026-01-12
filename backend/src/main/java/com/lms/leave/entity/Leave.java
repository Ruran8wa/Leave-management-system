package com.lms.leave.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Leave Entity - Represents leave requests in the database
 */
@Data
@Entity
@Table(name = "leaves")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Leave extends BaseEntity {
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;
    
    @Column(length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    @Column
    private Long approvedBy;  // User ID of approver
    
    @Column(length = 1000)
    private String approvalComments;  // Comments from manager/admin
    
    @Column(length = 500)
    private String rejectionReason;
    
    @Column(length = 500)
    private String documentPath;  // Path to uploaded document (if required)
    
    @Column(nullable = false)
    private Integer numberOfDays = 0;  // Calculated working days
}
