package com.lms.auth.entity;

import com.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STAFF;
    
    @Column
    private Long managerId;  // ID of the user's manager (for approval workflow)
    
    @Column
    private Boolean isActive = true;
    
    // 2FA fields
    @Column
    private String twoFactorSecret;
    
    @Column
    private Boolean twoFactorEnabled = false;
    
    // OAuth fields
    @Column
    private String oauthProvider;  // e.g., "google", "github"
    
    @Column
    private String oauthProviderId;
    
    @Column
    private String profilePictureUrl;
}
