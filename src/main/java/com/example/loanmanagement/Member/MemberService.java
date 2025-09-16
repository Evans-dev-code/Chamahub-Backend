package com.example.loanmanagement.Member;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import com.example.loanmanagement.Enum.ChamaRole;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChamaRepository chamaRepository;

    public MemberService(MemberRepository memberRepository,
                         UserRepository userRepository,
                         ChamaRepository chamaRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.chamaRepository = chamaRepository;
    }

    // ✅ Member joins chama via joinCode
    public MemberEntity joinChama(String userEmail, MemberDTO dto) {
        // 1. Fetch user by email
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + userEmail));

        // 2. Fetch chama by joinCode
        ChamaEntity chama = chamaRepository.findByJoinCode(dto.getJoinCode())
                .orElseThrow(() -> new RuntimeException("Invalid join code"));

        // 3. Prevent duplicate membership
        if (memberRepository.existsByUserAndChama(user, chama)) {
            throw new RuntimeException("User is already a member of this chama");
        }

        // 4. Create member entity
        MemberEntity member = new MemberEntity(
                dto.getPhoneNumber(),
                dto.getChamaRole() != null ? dto.getChamaRole() : ChamaRole.MEMBER,
                LocalDate.now(),
                user,
                chama
        );

        // 5. Save and return
        return memberRepository.save(member);
    }

    // ✅ Add new method in MemberService
    public MemberEntity addMember(MemberDTO dto) {
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        MemberEntity member = new MemberEntity(
                dto.getPhoneNumber(),
                dto.getChamaRole() != null ? dto.getChamaRole() : ChamaRole.MEMBER,
                dto.getJoinedDate() != null ? dto.getJoinedDate() : LocalDate.now(),
                user,
                null // ✅ no chama on approval
        );

        return memberRepository.save(member);
    }


    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }

    public List<MemberEntity> getMembersByChama(Long chamaId) {
        return memberRepository.findByChamaId(chamaId);
    }

    public Optional<MemberEntity> getMemberByUserId(Long userId) {
        return memberRepository.findByUserId(userId);
    }
}
