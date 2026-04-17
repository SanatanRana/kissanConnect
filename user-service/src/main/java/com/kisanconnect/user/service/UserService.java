package com.kisanconnect.user.service;

import com.kisanconnect.user.dto.LoginRequest;
import com.kisanconnect.user.dto.RegisterRequest;
import com.kisanconnect.user.entity.User;
import com.kisanconnect.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ========== Registration ==========
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // ENCRYPT PASSWORD
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setVillage(request.getVillage());
        user.setRole(request.getRole());
        user.setSellerType(request.getSellerType());

        return userRepository.save(user);
    }

    // ========== Login ==========
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    // ========== Update Profile ==========
    public User updateProfile(Long id, RegisterRequest request) {
        User user = getUserById(id);

        // Update allowed fields
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getVillage() != null) user.setVillage(request.getVillage());

        return userRepository.save(user);
    }

    // ========== Get User by ID ==========
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // ========== Get All Users ==========
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ========== Check if user exists (for inter-service calls) ==========
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}
