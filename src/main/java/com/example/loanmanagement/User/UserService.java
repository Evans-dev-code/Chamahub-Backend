package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public String registerUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username is already taken!";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email is already registered!";
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default to "USER" role if not set
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        userRepository.save(user);
        return "User registered successfully!";
    }

    public String authenticateUser(LoginRequest loginRequest) {
        UserEntity user;
        if (loginRequest.getIdentifier().contains("@")) {
            user = userRepository.findByEmail(loginRequest.getIdentifier())
                    .orElseThrow(() -> new RuntimeException("User not found by email"));
        } else {
            user = userRepository.findByUsername(loginRequest.getIdentifier())
                    .orElseThrow(() -> new RuntimeException("User not found by username"));
        }


        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtTokenUtil.generateToken(user);  // Token will contain role
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
}
