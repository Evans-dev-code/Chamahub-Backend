package com.example.loanmanagement.Member;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Enum.ChamaRole;
import com.example.loanmanagement.User.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamaRole chamaRole = ChamaRole.MEMBER;
    private LocalDate joinedDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-memberships")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chama_id", nullable = false)
    @JsonBackReference("chama-members")
    private ChamaEntity chama;



    public MemberEntity() {}

    public MemberEntity(String phoneNumber, ChamaRole chamaRole, LocalDate joinedDate, UserEntity user, ChamaEntity chama) {
        this.phoneNumber = phoneNumber;
        this.chamaRole = chamaRole;
        this.joinedDate = joinedDate;
        this.user = user;
        this.chama = chama;
    }
    @PrePersist
    protected void onCreate() {
        if (this.joinedDate == null) {
            this.joinedDate = LocalDate.now();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public ChamaRole getChamaRole() { return chamaRole; }
    public void setChamaRole(ChamaRole chamaRole) { this.chamaRole = chamaRole; }

    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public ChamaEntity getChama() { return chama; }
    public void setChama(ChamaEntity chama) { this.chama = chama; }
}
