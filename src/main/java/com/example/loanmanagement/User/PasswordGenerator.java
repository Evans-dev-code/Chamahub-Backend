package com.example.loanmanagement.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode("admin123");
        System.out.println("Hashed password: " + hashed);
    }
}
