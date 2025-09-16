package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Register a new user (default: role=USER, status=PENDING)
    public String registerUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username is already taken!";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email is already registered!";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.PENDING);
        }

        userRepository.save(user);
        return "User registered successfully!";
    }

    // Authenticate user and generate JWT
    public String authenticateUser(LoginRequest loginRequest) {
        UserEntity user = loginRequest.getIdentifier().contains("@")
                ? userRepository.findByEmail(loginRequest.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found by email"))
                : userRepository.findByUsername(loginRequest.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found by username"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtTokenUtil.generateToken(user);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ======================
    // Admin Methods
    // ======================

    public UserEntity approveUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        user.setStatus(UserStatus.APPROVED);
        return userRepository.save(user);
    }

    public boolean rejectUser(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus(UserStatus.REJECTED);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserEntity> getAllPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }
}
