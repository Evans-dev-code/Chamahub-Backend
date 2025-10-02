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

    @Autowired
    private EmailService emailService;  // ✅ Inject EmailService

    // ✅ Register new user
    public String registerUser(UserEntity user) {
        if (existsByUsername(user.getUsername())) {
            return "Username is already taken!";
        }
        if (existsByEmail(user.getEmail())) {
            return "Email is already registered!";
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default role + status
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.PENDING);
        }

        userRepository.save(user);

        // Send joining code (instead of calling private sendEmail directly)
        String joiningCode = "WELCOME-" + user.getId(); // example code, can generate random instead
        emailService.sendJoiningCode(user.getEmail(), joiningCode);

        return "User registered successfully!";
    }

    // ✅ Authenticate user
    public String authenticateUser(LoginRequest loginRequest) {
        UserEntity user = getUserByUsernameOrEmail(loginRequest.getIdentifier());
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtTokenUtil.generateToken(user);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    // ✅ Get user by username
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Get user by email
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Get user by username or email
    public UserEntity getUserByUsernameOrEmail(String identifier) {
        if (identifier.contains("@")) {
            return getUserByEmail(identifier);
        } else {
            return getUserByUsername(identifier);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ✅ Approve user + send email
    public UserEntity approveUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setStatus(UserStatus.APPROVED);
        userRepository.save(user);

        emailService.notifyUserLoanDecision(user.getEmail(), user.getUsername(), true);

        return user;
    }

    // ✅ Reject user + send email
    public boolean rejectUser(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus(UserStatus.REJECTED);
            userRepository.save(user);

            emailService.notifyUserLoanDecision(user.getEmail(), user.getUsername(), false);

            return true;
        }).orElse(false);
    }

    // ✅ Delete user + send email
    public boolean deleteUser(Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);

            // Use generic email for deletion (since EmailService doesn’t yet have this method, you can add one if you want)
            emailService.sendContributionReminder(
                    user.getEmail(),
                    0,
                    "Account Deleted"
            );

            return true;
        }).orElse(false);
    }

    // ✅ Get all pending users
    public List<UserEntity> getAllPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }
}
