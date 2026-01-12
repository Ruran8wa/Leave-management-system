package com.lms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTOs (Data Transfer Objects) for Auth requests/responses
 */
public class AuthDto {
    
    /**
     * Register Request DTO
     * Note: Role is not included - all new users are registered as STAFF by default for security
     * Admins must manually upgrade user roles through the admin panel
     */
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        
        @NotBlank(message = "First name is required")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        private String lastName;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        private String twoFactorCode; // Optional, for 2FA verification
    }
    
    @Data
    public static class AuthResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String token; // JWT token
        private String profilePictureUrl;
        private Boolean requires2FA; // Indicates if 2FA verification is needed
        private String message;
    }
    
    @Data
    public static class TwoFactorSetupResponse {
        private String secret;
        private String qrCodeUrl;
        private String message;
    }
    
    @Data
    public static class TwoFactorVerifyRequest {
        @NotBlank(message = "Verification code is required")
        private String code;
    }
}
