package com.example.loanmanagement.Chama;

public class AddMemberRequest {
    private String email;
    private String phoneNumber;
    private String chamaRole;

    public AddMemberRequest() {}

    public AddMemberRequest(String email, String phoneNumber, String chamaRole) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.chamaRole = chamaRole;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getChamaRole() { return chamaRole; }
    public void setChamaRole(String chamaRole) { this.chamaRole = chamaRole; }
}
