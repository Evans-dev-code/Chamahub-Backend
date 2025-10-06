package com.example.loanmanagement.Chama;

import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.Enum.ChamaRole;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChamaService {

    private final ChamaRepository chamaRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    public ChamaService(ChamaRepository chamaRepository, UserRepository userRepository, MemberRepository memberRepository) {
        this.chamaRepository = chamaRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ChamaEntity createChama(CreateChamaRequest request, Long userId) {
        UserEntity creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (chamaRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chama name already taken");
        }

        String joinCode = generateUniqueCode();

        ChamaEntity chama = new ChamaEntity();
        chama.setName(request.getName());
        chama.setDescription(request.getDescription());
        chama.setCreatedBy(creator);
        chama.setJoinCode(joinCode);

        chamaRepository.save(chama);

        MemberEntity member = new MemberEntity();
        member.setUser(creator);
        member.setChama(chama);
        member.setChamaRole(ChamaRole.ADMIN);
        memberRepository.save(member);

        return chama;
    }

    @Transactional
    public ChamaEntity joinChama(String joinCode, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ChamaEntity chama = chamaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid join code"));

        boolean alreadyMember = memberRepository.existsByUserAndChama(user, chama);
        if (alreadyMember) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already a member of this chama");
        }

        MemberEntity member = new MemberEntity();
        member.setUser(user);
        member.setChama(chama);
        member.setChamaRole(ChamaRole.MEMBER);
        memberRepository.save(member);

        return chama;
    }

    @Transactional(readOnly = true)
    public List<ChamaEntity> getChamasForUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<MemberEntity> memberships = memberRepository.findAllByUser(user);

        return memberships.stream()
                .map(MemberEntity::getChama)
                .collect(Collectors.toList());
    }

    @Transactional
    public String regenerateJoinCode(Long chamaId, Long requesterId) {
        ChamaEntity chama = chamaRepository.findById(chamaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chama not found"));

        UserEntity requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (chama.getCreatedBy() == null || !chama.getCreatedBy().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to regenerate the join code for this chama");
        }

        String newCode = generateUniqueCode();
        chama.setJoinCode(newCode);
        chamaRepository.save(chama);

        return newCode;
    }

    private String generateUniqueCode() {
        return "CHAMA-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
