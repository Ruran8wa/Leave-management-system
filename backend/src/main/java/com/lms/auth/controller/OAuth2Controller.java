package com.lms.auth.controller;

import com.lms.auth.dto.AuthDto;
import com.lms.auth.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "Google OAuth2 authentication callback endpoints")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/success")
    public ResponseEntity<AuthDto.AuthResponse> oauthSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            String email = oAuth2User.getAttribute("email");
            AuthDto.AuthResponse response = oAuth2Service.handleOAuth2Success(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/failure")
    public ResponseEntity<AuthDto.AuthResponse> oauthFailure() {
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setMessage("OAuth2 login failed");
        return ResponseEntity.badRequest().body(response);
    }
}
