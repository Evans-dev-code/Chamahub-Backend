package com.example.loanmanagement.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    private final String fromEmail = "yourgmail@gmail.com"; // ✅ should match spring.mail.username in application.properties

    /**
     * Generic reusable method to send an email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            logger.info("📧 Email sent successfully -> To: {}, Subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("❌ Failed to send email -> To: {}, Subject: {}, Error: {}", to, subject, e.getMessage(), e);
        }
    }

    /**
     * Alias method so other services can call sendGenericEmail()
     */
    public void sendGenericEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    // === Specific emails ===

    public void sendJoiningCode(String userEmail, String joiningCode) {
        String subject = "Welcome to ChamaHub 🎉";
        String body = "Hello,\n\nYour joining code is: " + joiningCode +
                "\nUse this code to activate your account.\n\nBest regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void sendContributionReminder(String userEmail, double amount, String dueDate) {
        String subject = "Contribution Reminder ⏰";
        String body = "Hello,\n\nThis is a reminder that your contribution of Ksh " + amount +
                " is due on " + dueDate + ".\nPlease make your payment before the deadline.\n\nThank you,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void notifyAdminLoanApplication(String adminEmail, String userName, double amount) {
        String subject = "📩 New Loan Application";
        String body = "Dear Admin,\n\nUser " + userName +
                " has applied for a loan of Ksh " + amount +
                ".\nPlease review the application in the system.\n\nChamaHub System";
        sendEmail(adminEmail, subject, body);
    }

    public void notifyUserLoanDecision(String userEmail, String userName, boolean approved) {
        String subject = "Loan Application Result";
        String status = approved ? "approved ✅" : "rejected ❌";
        String body = "Hello " + userName + ",\n\nYour loan application has been " + status +
                ".\nPlease log in to the system for more details.\n\nBest regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void sendWelcomeEmail(String userEmail, String username) {
        String subject = "Welcome to ChamaHub 🎉";
        String body = "Hello " + username + ",\n\n" +
                "Your account has been created successfully in ChamaHub. " +
                "Please wait for admin approval before accessing the system.\n\n" +
                "Best regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void sendApprovalEmail(String userEmail, String username) {
        String subject = "Your ChamaHub Account Has Been Approved ✅";
        String body = "Hello " + username + ",\n\n" +
                "Good news! Your account has been approved by the admin. " +
                "You can now log in and start using ChamaHub.\n\n" +
                "Best regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void sendRejectionEmail(String userEmail, String username) {
        String subject = "Your ChamaHub Account Request Was Rejected ❌";
        String body = "Hello " + username + ",\n\n" +
                "We’re sorry, but your account request has been rejected by the admin. " +
                "If you believe this was a mistake, please contact support.\n\n" +
                "Best regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }

    public void sendDeletionEmail(String userEmail, String username) {
        String subject = "Your ChamaHub Account Has Been Deleted ⚠️";
        String body = "Hello " + username + ",\n\n" +
                "We regret to inform you that your ChamaHub account has been deleted. " +
                "If you have questions, please reach out to our support team.\n\n" +
                "Best regards,\nChamaHub Team";
        sendEmail(userEmail, subject, body);
    }
}
