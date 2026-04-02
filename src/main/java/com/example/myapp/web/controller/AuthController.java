package com.example.myapp.web.controller;

import com.example.myapp.security.JwtTokenProvider;
import com.example.myapp.web.dto.AuthRequest;
import com.example.myapp.web.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Get a JWT token (demo — accepts any credentials)")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        // TODO: Replace with real authentication (UserDetailsService + PasswordEncoder)
        var token = tokenProvider.generateToken(request.username());
        return new AuthResponse(token);
    }
}
