package com.lms.auth.entity;

/**
 * User Roles in the system
 */
public enum Role {
    STAFF("Staff", "Regular employee"),
    MANAGER("Manager", "Can approve/reject leave requests"),
    ADMIN("Admin", "Full system access, can manage leave policies");
    
    private final String displayName;
    private final String description;
    
    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
