package com.example.loanmanagement.Chama;

import com.example.loanmanagement.Chama.ChamaDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chamas")
public class ChamaController {

    private final ChamaService chamaService;

    @Autowired
    public ChamaController(ChamaService chamaService) {
        this.chamaService = chamaService;
    }

    /**
     * ✅ Admin creates a chama
     */
    @PostMapping("/create")
    public ResponseEntity<ChamaDto> createChama(
            @RequestBody CreateChamaRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId"); // 👈 extracted by JwtTokenFilter
        ChamaEntity createdChama = chamaService.createChama(request, userId);
        return ResponseEntity.ok(ChamaDto.fromEntity(createdChama));
    }

    /**
     * ✅ User joins chama with joinCode
     */
    @PostMapping("/join")
    public ResponseEntity<ChamaDto> joinChama(
            @RequestParam String joinCode,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ChamaEntity joinedChama = chamaService.joinChama(joinCode, userId);
        return ResponseEntity.ok(ChamaDto.fromEntity(joinedChama));
    }

    /**
     * ✅ Get all chamas the logged-in user belongs to
     */
    @GetMapping("/my-chamas")
    public ResponseEntity<List<ChamaDto>> getMyChamas(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ChamaEntity> chamas = chamaService.getChamasForUser(userId);

        List<ChamaDto> dtoList = chamas.stream()
                .map(ChamaDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /**
     * ✅ Regenerate join code (admin only)
     */
    @PostMapping("/{id}/generate-join-code")
    public ResponseEntity<Map<String, String>> regenerateJoinCode(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String newCode = chamaService.regenerateJoinCode(id, userId);
        return ResponseEntity.ok(Map.of("joinCode", newCode));
    }
}
