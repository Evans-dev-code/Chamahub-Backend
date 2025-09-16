package com.example.loanmanagement.User;

import com.example.loanmanagement.Enum.ChamaRole;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserService;
import com.example.loanmanagement.Member.MemberDTO;
import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    // ✅ Approve a user and convert them into a Member
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        Optional<UserEntity> userOpt = Optional.ofNullable(userService.approveUser(id));
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = userOpt.get();

        // Create MemberDTO
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(user.getId());
        memberDTO.setPhoneNumber("0712345678"); // replace with real data
        memberDTO.setChamaRole(ChamaRole.MEMBER); // ✅ enum instead of string
        memberDTO.setJoinedDate(LocalDate.now());

        MemberEntity savedMember = memberService.addMember(memberDTO);

        return ResponseEntity.ok("User approved and added as member with ID: " + savedMember.getId());
    }

    // Reject a user registration
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        boolean rejected = userService.rejectUser(id);
        if (rejected) {
            return ResponseEntity.ok("User rejected successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a user entirely
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all pending user accounts for review
    @GetMapping("/pending")
    public ResponseEntity<List<UserEntity>> getPendingUsers() {
        return ResponseEntity.ok(userService.getAllPendingUsers());
    }
}
