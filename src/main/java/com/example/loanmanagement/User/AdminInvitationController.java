package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-invitations")
public class AdminInvitationController {

    @Autowired
    private AdminInvitationService invitationService;

    @Autowired
    private UserService userService;

    /**
     * Endpoint for the super admin to invite another admin
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteAdmin(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String email = request.get("email");
        String inviterUsername = authentication.getName();
        UserEntity inviter = userService.getUserByUsername(inviterUsername);

        if (!inviter.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("message", "❌ Only super admins can invite new admins."));
        }

        String result = invitationService.inviteAdmin(email, inviter);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Endpoint to accept invitation and create an admin account
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String fullName = request.get("fullName");
        String username = request.get("username");
        String password = request.get("password");

        String result = invitationService.acceptInvitation(token, fullName, username, password);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
