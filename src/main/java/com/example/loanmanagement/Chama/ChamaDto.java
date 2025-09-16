package com.example.loanmanagement.Chama;

import com.example.loanmanagement.Chama.ChamaEntity;

import java.time.LocalDate;

public record ChamaDto(
        Long id,
        String name,
        String description,
        LocalDate createdDate,
        String joinCode,
        String createdByName // instead of exposing full UserEntity
) {
    public static ChamaDto fromEntity(ChamaEntity chama) {
        return new ChamaDto(
                chama.getId(),
                chama.getName(),
                chama.getDescription(),
                chama.getCreatedDate(),
                chama.getJoinCode(),
                chama.getCreatedBy() != null ? chama.getCreatedBy().getUsername() : null
        );
    }
}

