package com.lms.leave.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "leaveType", "leave_year"})
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaveBalance extends BaseEntity {
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;
    
    @Column(name = "leave_year", nullable = false)
    private Integer year;  // e.g., 2026
    
    @Column(nullable = false)
    private Integer totalAllocated;  // Days allocated for the year
    
    @Column(nullable = false)
    private Integer used = 0;  // Days used so far
    
    @Column(nullable = false)
    private Integer pending = 0;  // Days in pending requests
    
    @Column(nullable = false)
    private Integer carriedOver = 0;  // Days carried over from previous year
    
    @Column
    private LocalDate carryOverExpiryDate;  // When carried over days expire (Jan 31)
    
    public Integer getRemaining() {
        return totalAllocated - used - pending;
    }
    
    public Integer getAvailable() {
        int available = getRemaining();
        if (carriedOver > 0 && carryOverExpiryDate != null && 
            LocalDate.now().isBefore(carryOverExpiryDate)) {
            available += carriedOver;
        }
        return available;
    }
    
    public boolean canRequest(Integer days) {
        return getAvailable() >= days;
    }
}
