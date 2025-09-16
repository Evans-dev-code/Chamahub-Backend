package com.example.loanmanagement.Member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ✅ Self-join using joinCode
    @PostMapping("/join")
    public ResponseEntity<MemberEntity> joinChama(@RequestBody MemberDTO dto, Authentication auth) {
        String email = auth.getName(); // logged-in user
        MemberEntity member = memberService.joinChama(email, dto);
        return ResponseEntity.ok(member);
    }

    @GetMapping
    public ResponseEntity<List<MemberEntity>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<List<MemberEntity>> getByChama(@PathVariable Long chamaId) {
        return ResponseEntity.ok(memberService.getMembersByChama(chamaId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<MemberEntity> getByUserId(@PathVariable Long userId) {
        return memberService.getMemberByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
