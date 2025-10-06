package com.example.loanmanagement.Chama;

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

    @PostMapping("/create")
    public ResponseEntity<ChamaDto> createChama(
            @RequestBody CreateChamaRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ChamaEntity createdChama = chamaService.createChama(request, userId);
        return ResponseEntity.ok(ChamaDto.fromEntity(createdChama));
    }

    @PostMapping("/join")
    public ResponseEntity<ChamaDto> joinChama(
            @RequestParam String joinCode,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ChamaEntity joinedChama = chamaService.joinChama(joinCode, userId);
        return ResponseEntity.ok(ChamaDto.fromEntity(joinedChama));
    }

    @GetMapping("/my-chamas")
    public ResponseEntity<List<ChamaDto>> getMyChamas(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ChamaEntity> chamas = chamaService.getChamasForUser(userId);
        List<ChamaDto> dtoList = chamas.stream()
                .map(ChamaDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

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
