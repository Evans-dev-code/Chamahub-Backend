package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminInvitationService {

    @Autowired
    private AdminInvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Send an invitation email with token
     */
    public String inviteAdmin(String email, UserEntity invitedBy) {
        if (userRepository.existsByEmail(email)) {
            return "❌ A user with this email already exists.";
        }

        if (invitationRepository.existsByEmailAndAcceptedFalse(email)) {
            return "⚠️ An invitation has already been sent to this email.";
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(2); // valid for 48 hours

        AdminInvitationEntity invitation = new AdminInvitationEntity(email, token, expiryDate, invitedBy);
        invitationRepository.save(invitation);

        // Build activation link
        String inviteLink = "http://localhost:4200/invite-accept?token=" + token;

        String subject = "📩 Invitation to Become a Chama Admin";
        String body = "Hello,\n\n" +
                "You’ve been invited by " + invitedBy.getFullName() + " to become an admin on ChamaHub.\n\n" +
                "To accept and create your admin account, click the link below:\n" +
                inviteLink + "\n\n" +
                "⚠️ Note: This link will expire in 48 hours.\n\n" +
                "Best regards,\nChamaHub Team";

        emailService.sendEmail(email, subject, body);

        return "✅ Invitation sent successfully to " + email;
    }

    /**
     * Accept invitation and create admin account
     */
    public String acceptInvitation(String token, String fullName, String username, String password) {
        Optional<AdminInvitationEntity> invitationOpt = invitationRepository.findByToken(token);

        if (invitationOpt.isEmpty()) {
            return "❌ Invalid or expired invitation token.";
        }

        AdminInvitationEntity invitation = invitationOpt.get();

        if (invitation.isAccepted() || invitation.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "❌ Invitation already used or expired.";
        }

        // Create new admin user
        UserEntity newAdmin = new UserEntity();
        newAdmin.setFullName(fullName);
        newAdmin.setEmail(invitation.getEmail());
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password));
        newAdmin.setRole("ADMIN");
        newAdmin.setStatus(UserStatus.APPROVED);

        userRepository.save(newAdmin);

        // Mark invitation as accepted
        invitation.setAccepted(true);
        invitationRepository.save(invitation);

        return "✅ Admin account created successfully for " + invitation.getEmail();
    }
}
