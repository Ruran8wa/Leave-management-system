package com.lms.leave.entity;

public enum LeaveStatus {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    LeaveStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
