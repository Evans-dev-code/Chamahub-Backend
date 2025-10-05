package com.example.loanmanagement.Passwordreset;

import com.example.loanmanagement.User.EmailService;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final long EXPIRATION_MINUTES = 15;

    public void createPasswordResetToken(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        UserEntity user = userOpt.get();
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(token, user, expiryDate);
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired");
        }

        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                "Password Successfully Reset ✅",
                "Hello " + user.getFullName() + ",\n\n" +
                        "Your password has been changed successfully.\n\n" +
                        "If you didn’t make this change, please contact support immediately.\n\n" +
                        "Best regards,\nChamaHub Team"
        );
    }
}
