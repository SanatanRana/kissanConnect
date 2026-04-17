package com.kisanconnect.user.controller;

import com.kisanconnect.user.dto.LoginRequest;
import com.kisanconnect.user.dto.RegisterRequest;
import com.kisanconnect.user.entity.User;
import com.kisanconnect.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final com.kisanconnect.user.security.JwtUtil jwtUtil;

    // ========== Register Farmer / Shopkeeper / Customer ==========
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "userId", user.getId(),
                    "role", user.getRole().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Login ==========
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            
            String userType = user.getSellerType() != null ? user.getSellerType().name() : "NONE";
            String token = jwtUtil.generateToken(user.getId(), user.getRole().name(), userType);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token,
                    "userId", user.getId(),
                    "role", user.getRole().name(),
                    "name", user.getName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get User Profile ==========
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Update Profile ==========
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody RegisterRequest request) {
        try {
            User updated = userService.updateProfile(id, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "userId", updated.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get All Users ==========
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ========== Health Check (for inter-service validation) ==========
    @GetMapping("/users/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        return ResponseEntity.ok(userService.existsById(id));
    }
}
