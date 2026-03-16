package com.devplus.controller;

import com.devplus.dto.LoginRequest;
import com.devplus.dto.LoginResponse;
import com.devplus.dto.RegisterRequest;
import com.devplus.model.Role;
import com.devplus.model.User;
import com.devplus.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid email or password"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
    
    // Response DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private String githubUsername;
        
        public static UserResponse fromUser(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .githubUsername(user.getGithubUsername())
                    .build();
        }
    }
}