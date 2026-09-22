package com.example.sporty.features.exerciseMatching.domain.dto;

import java.time.LocalDateTime;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class MatchResponseDto {

    private Integer matchId;

    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer maxParticipant;

    private MatchStatus status;
    private SkillLevel skillLevel;
    private SportType sportType;

    public static MatchResponseDto toResponseDto(MatchEntity entity) {
        return MatchResponseDto.builder()
            .matchId(entity.getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .startAt(entity.getStartAt())
            .endAt(entity.getEndAt())
            .maxParticipant(entity.getMaxParticipant())
            .status(entity.getStatus())
            .skillLevel(entity.getSkillLevel())
            .sportType(entity.getSportType())
            .build();
    }
}