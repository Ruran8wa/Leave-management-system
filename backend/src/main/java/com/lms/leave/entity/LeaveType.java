package com.lms.leave.entity;

public enum LeaveType {
    PERSONAL_TIME_OFF("Personal Time Off", 20),  // 20 days per year
    SICK_LEAVE("Sick Leave", null),               // No limit
    COMPASSIONATE_LEAVE("Compassionate Leave", null),
    MATERNITY_LEAVE("Maternity Leave", null),
    PATERNITY_LEAVE("Paternity Leave", null),
    ANNUAL_LEAVE("Annual Leave", null),
    UNPAID_LEAVE("Unpaid Leave", null);
    
    private final String displayName;
    private final Integer annualAllocation;  // Days allocated per year (null = no limit)
    
    LeaveType(String displayName, Integer annualAllocation) {
        this.displayName = displayName;
        this.annualAllocation = annualAllocation;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Integer getAnnualAllocation() {
        return annualAllocation;
    }
    
    public boolean hasAllocation() {
        return annualAllocation != null;
    }
}
