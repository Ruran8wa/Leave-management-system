package com.lms.auth.service;

import com.lms.auth.dto.AuthDto;
import com.lms.auth.entity.Role;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 Service - Handles Google OAuth2 login
 */
@Service
@RequiredArgsConstructor
public class OAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Process OAuth2 user and save/update in database
        processOAuth2User(userRequest, oAuth2User);
        
        return oAuth2User;
    }

    private void processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String pictureUrl = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");

        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            if (user.getOauthProvider() == null) {
                user.setOauthProvider(registrationId);
                user.setOauthProviderId(providerId);
            }
            user.setProfilePictureUrl(pictureUrl);
            userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName != null ? firstName : "");
            newUser.setLastName(lastName != null ? lastName : "");
            newUser.setPassword(""); // OAuth users don't need password
            newUser.setRole(Role.STAFF);
            newUser.setIsActive(true);
            newUser.setOauthProvider(registrationId);
            newUser.setOauthProviderId(providerId);
            newUser.setProfilePictureUrl(pictureUrl);
            newUser.setTwoFactorEnabled(false);
            userRepository.save(newUser);
        }
    }

    public AuthDto.AuthResponse handleOAuth2Success(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        response.setMessage("OAuth2 login successful");

        return response;
    }
}
