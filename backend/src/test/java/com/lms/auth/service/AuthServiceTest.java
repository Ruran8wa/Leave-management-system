package com.lms.auth.service;

import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Authentication Service Tests")
class AuthServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    private AuthDto.RegisterRequest registerRequest;
    private AuthDto.LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.STAFF);
        testUser.setIsActive(true);
        testUser.setTwoFactorEnabled(false);
    }

    @Test
    @DisplayName("Should successfully register a new user with STAFF role")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthDto.AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("STAFF", response.getRole());
        assertNotNull(response.getToken()); // Real JWT token generated
        
        // Verify user is saved with STAFF role (security check)
        verify(userRepository).save(argThat(user -> 
            user.getRole() == Role.STAFF && 
            user.getIsActive() == true &&
            user.getTwoFactorEnabled() == false
        ));
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void testRegisterDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        AuthDto.AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertNotNull(response.getToken()); // Real JWT token generated
        assertNull(response.getRequires2FA()); // 2FA is disabled
    }

    @Test
    @DisplayName("Should throw exception when login with invalid email")
    void testLoginInvalidEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    @DisplayName("Should throw exception when login with invalid password")
    void testLoginInvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    @DisplayName("Should require 2FA when user has 2FA enabled")
    void testLoginWith2FAEnabled() {
        // Arrange
        testUser.setTwoFactorEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        AuthDto.AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getRequires2FA());
        assertNull(response.getToken()); // No token until 2FA verification
    }

    @Test
    @DisplayName("Should throw exception when login with inactive account")
    void testLoginInactiveAccount() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertTrue(exception.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("Should ensure password is encoded before saving")
    void testPasswordEncoding() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> 
            user.getPassword().equals("$2a$10$encodedPassword")
        ));
    }

    @Test
    @DisplayName("Should enforce STAFF role for all new registrations (security test)")
    void testRegistrationAlwaysCreatesStaffRole() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(registerRequest);

        // Assert - Critical security check
        verify(userRepository).save(argThat(user -> {
            // Ensure user cannot set their own role to ADMIN or MANAGER
            assertEquals(Role.STAFF, user.getRole(), 
                "Security Violation: New users must always be STAFF role");
            return true;
        }));
    }
}
