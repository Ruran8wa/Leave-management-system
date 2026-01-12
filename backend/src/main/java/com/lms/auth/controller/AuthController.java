package com.lms.auth.controller;

import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.User;
import com.lms.auth.service.AuthService;
import com.lms.auth.service.TwoFactorAuthService;
import com.lms.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints including JWT login, 2FA, and OAuth2")
public class AuthController {
    
    // Injected by Spring (like in NestJS)
    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtil jwtUtil;
    
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with email, password, and basic information. All new users are registered as STAFF by default. Password is automatically hashed with BCrypt. Admins can upgrade user roles later."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        try {
            AuthDto.AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password. Returns JWT token if 2FA is disabled. If 2FA is enabled, requires twoFactorCode in request."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned or 2FA required"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or 2FA code")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            // If 2FA code is provided, verify it
            if (request.getTwoFactorCode() != null && !request.getTwoFactorCode().isEmpty()) {
                AuthDto.AuthResponse response = twoFactorAuthService.verifyTwoFactorCode(
                        request.getEmail(), request.getTwoFactorCode());
                return ResponseEntity.ok(response);
            }
            
            // Regular login
            AuthDto.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Operation(
        summary = "Setup Two-Factor Authentication",
        description = "Generates a QR code for Google Authenticator setup. User must scan the QR code and verify with a code to enable 2FA.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "QR code generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @PostMapping("/2fa/setup")
    public ResponseEntity<AuthDto.TwoFactorSetupResponse> setupTwoFactor(Authentication authentication) {
        try {
            String email = authentication.getName();
            Long userId = jwtUtil.extractUserId(extractToken(authentication));
            AuthDto.TwoFactorSetupResponse response = twoFactorAuthService.setupTwoFactor(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @PostMapping("/2fa/enable")
    public ResponseEntity<AuthDto.AuthResponse> enableTwoFactor(
            @Valid @RequestBody AuthDto.TwoFactorVerifyRequest request,
            Authentication authentication) {
        try {
            Long userId = jwtUtil.extractUserId(extractToken(authentication));
            AuthDto.AuthResponse response = twoFactorAuthService.enableTwoFactor(userId, request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @PostMapping("/2fa/disable")
    public ResponseEntity<AuthDto.AuthResponse> disableTwoFactor(
            @Valid @RequestBody AuthDto.TwoFactorVerifyRequest request,
            Authentication authentication) {
        try {
            Long userId = jwtUtil.extractUserId(extractToken(authentication));
            AuthDto.AuthResponse response = twoFactorAuthService.disableTwoFactor(userId, request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = authService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    private String extractToken(Authentication authentication) {
        // In a real implementation, you'd get this from the request header
        // For now, this is a placeholder
        return null;
    }
}