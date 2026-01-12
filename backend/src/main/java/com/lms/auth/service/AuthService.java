package com.lms.auth.service;

import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.common.exception.UserAlreadyExistsException;
import com.lms.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password with BCrypt
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        // Security: Always set new users as STAFF - admins must manually upgrade roles
        user.setRole(Role.STAFF);
        user.setIsActive(true);
        user.setTwoFactorEnabled(false);
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());
        
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setRole(savedUser.getRole().name());
        response.setToken(token);
        response.setMessage("User registered successfully");
        
        return response;
    }
    
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }
        
        // If 2FA is enabled, require 2FA code
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            AuthDto.AuthResponse response = new AuthDto.AuthResponse();
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setRequires2FA(true);
            response.setMessage("2FA verification required");
            return response;
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole().name());
        response.setToken(token);
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setMessage("Login successful");
        
        return response;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
