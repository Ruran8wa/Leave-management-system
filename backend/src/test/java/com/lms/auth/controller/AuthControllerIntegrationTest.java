package com.lms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should successfully register a new user")
    void testRegisterSuccess() throws Exception {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("STAFF")) // Security check
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        // Arrange - Create existing user
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.STAFF);
        existingUser.setIsActive(true);
        userRepository.save(existingUser);

        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    assertTrue(result.getResolvedException() instanceof RuntimeException);
                    assertTrue(result.getResolvedException().getMessage().contains("already exists"));
                });
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail with invalid email")
    void testRegisterInvalidEmail() throws Exception {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail with short password")
    void testRegisterShortPassword() throws Exception {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("123"); // Too short
        request.setFirstName("John");
        request.setLastName("Doe");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail with missing fields")
    void testRegisterMissingFields() throws Exception {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("user@example.com");
        // Missing password, firstName, lastName

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should successfully login with valid credentials")
    void testLoginSuccess() throws Exception {
        // Arrange - Create user
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STAFF);
        user.setIsActive(true);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should fail with invalid email")
    void testLoginInvalidEmail() throws Exception {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    assertTrue(result.getResolvedException() instanceof RuntimeException);
                    assertTrue(result.getResolvedException().getMessage().contains("Invalid"));
                });
    }

    @Test
    @DisplayName("POST /api/auth/login - Should fail with invalid password")
    void testLoginInvalidPassword() throws Exception {
        // Arrange - Create user
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STAFF);
        user.setIsActive(true);
        userRepository.save(user);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    assertTrue(result.getResolvedException() instanceof RuntimeException);
                    assertTrue(result.getResolvedException().getMessage().contains("Invalid"));
                });
    }

    @Test
    @DisplayName("POST /api/auth/login - Should require 2FA when enabled")
    void testLoginWith2FAEnabled() throws Exception {
        // Arrange - Create user with 2FA enabled
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STAFF);
        user.setIsActive(true);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret("test-secret");
        userRepository.save(user);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requires2FA").value(true))
                .andExpect(jsonPath("$.token").doesNotExist()); // No token until 2FA
    }

    @Test
    @DisplayName("Security Test - New users are always STAFF role")
    void testSecurityNewUsersAlwaysStaff() throws Exception {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        // Note: No role field in request - it shouldn't exist

        // Act
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STAFF"));

        // Assert - Verify in database
        User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assert savedUser.getRole() == Role.STAFF : "Security violation: User created with non-STAFF role";
    }
}
