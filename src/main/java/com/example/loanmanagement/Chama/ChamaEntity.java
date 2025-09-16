package com.example.loanmanagement.Chama;

import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamas")
public class ChamaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDate createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
    }

    @Column(unique = true)
    private String joinCode; // e.g., "CHAMA-123ABC"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @OneToMany(mappedBy = "chama", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("chama-members")
    private List<MemberEntity> members = new ArrayList<>();

    // Constructors
    public ChamaEntity() {}

    public ChamaEntity(String name, String description, UserEntity createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public UserEntity getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserEntity createdBy) { this.createdBy = createdBy; }

    public List<MemberEntity> getMembers() { return members; }
    public void setMembers(List<MemberEntity> members) { this.members = members; }

    public void addMember(MemberEntity member) {
        members.add(member);
        member.setChama(this);
    }
}
