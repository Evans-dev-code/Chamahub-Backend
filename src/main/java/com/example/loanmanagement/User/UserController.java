package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserEntity user) {
        String result = userService.registerUser(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user and get token
            UserEntity user = userService.getUserByUsername(loginRequest.getUsernameOrEmail());
            String token = userService.authenticateUser(loginRequest);

            // Build secure response (password already stripped in LoginResponse)
            return ResponseEntity.ok(new LoginResponse(token, user));

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(new LoginResponse(401, "Unauthorized: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable String username,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        try {
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body(new LoginResponse(401, "Missing or invalid Authorization header"));
            }

            String token = tokenHeader.substring(7); // Remove "Bearer " prefix
            String usernameFromToken = jwtTokenUtil.extractUsername(token);

            if (!usernameFromToken.equals(username)) {
                return ResponseEntity.status(403)
                        .body(new LoginResponse(403, "Forbidden - Username mismatch"));
            }

            UserEntity user = userService.getUserByUsername(username);
            return ResponseEntity.ok(new LoginResponse(token, user));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new LoginResponse(401, "Unauthorized: " + e.getMessage()));
        }
    }
}
