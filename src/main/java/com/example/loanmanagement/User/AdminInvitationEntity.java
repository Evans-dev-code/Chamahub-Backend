package com.example.loanmanagement.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_invitations")
public class AdminInvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // invited admin's email

    @Column(nullable = false, unique = true)
    private String token; // unique invitation token

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean accepted = false;

    @ManyToOne
    @JoinColumn(name = "invited_by_id")
    private UserEntity invitedBy; // the super admin who sent this invitation

    public AdminInvitationEntity() {}

    public AdminInvitationEntity(String email, String token, LocalDateTime expiryDate, UserEntity invitedBy) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
        this.invitedBy = invitedBy;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public UserEntity getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UserEntity invitedBy) {
        this.invitedBy = invitedBy;
    }
}
