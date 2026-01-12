package com.lms.auth.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.config.JwtUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Two-Factor Authentication Service using Google Authenticator
 */
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator googleAuthenticator;
    private final JwtUtil jwtUtil;

    /**
     * Generate 2FA secret and QR code for user
     */
    @Transactional
    public AuthDto.TwoFactorSetupResponse setupTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new secret
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        // Save secret to user (but don't enable 2FA yet)
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        // Generate QR code URL
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "LMS-Application",
                user.getEmail(),
                key
        );

        // Generate QR code image as base64
        String qrCodeImage = generateQRCodeImage(qrCodeUrl);

        AuthDto.TwoFactorSetupResponse response = new AuthDto.TwoFactorSetupResponse();
        response.setSecret(secret);
        response.setQrCodeUrl(qrCodeImage);
        response.setMessage("Scan this QR code with Google Authenticator app");

        return response;
    }

    /**
     * Enable 2FA after verifying the code
     */
    @Transactional
    public AuthDto.AuthResponse enableTwoFactor(Long userId, String verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("2FA not set up. Please set up 2FA first.");
        }

        // Verify the code
        boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), Integer.parseInt(verificationCode));
        
        if (!isValid) {
            throw new RuntimeException("Invalid verification code");
        }

        // Enable 2FA
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setMessage("Two-factor authentication enabled successfully");
        return response;
    }

    /**
     * Disable 2FA
     */
    @Transactional
    public AuthDto.AuthResponse disableTwoFactor(Long userId, String verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA is not enabled");
        }

        // Verify the code before disabling
        boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), Integer.parseInt(verificationCode));
        
        if (!isValid) {
            throw new RuntimeException("Invalid verification code");
        }

        // Disable 2FA
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setMessage("Two-factor authentication disabled successfully");
        return response;
    }

    /**
     * Verify 2FA code during login
     */
    public AuthDto.AuthResponse verifyTwoFactorCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA is not enabled for this user");
        }

        // Verify the code
        boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), Integer.parseInt(code));
        
        if (!isValid) {
            throw new RuntimeException("Invalid verification code");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        // Return response
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

    /**
     * Generate QR code image as base64 string
     */
    private String generateQRCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
